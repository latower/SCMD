package propagator

import oscar.cp._
import oscar.algo.Inconsistency
import java.io.File

object RunnerMaxExpFull extends App with CPModel {

  private[this] var numberOfMaxVars = -1;
  private[this] var solution: Array[Int] = null;
  private[this] var varMap: Array[Int] = null;

  val parser = argsParser()
  parser.parse(args, Config()) match {
    case Some(config) =>
      System.err.println("Start MaxExpFull on " + config.bddFile.getAbsolutePath)

      val bdd = new Wbdd(config.bddFile.getAbsolutePath)
      val card = config.maxcard

      numberOfMaxVars = bdd.numberOfMaxVars
      varMap = bdd.decMapCpToBdd

      val X = Array.fill(numberOfMaxVars)(CPBoolVar())
      solution = Array.fill(numberOfMaxVars)(-1);

      val wbddC = new WbddConstraintFull(bdd, X, 0.0)

      try {
        add(sum(X) <= card)
        add(wbddC)
      } catch {
        case _: NoSolutionException => { println("infeasible"); System.exit(1) }
      }

      var objVal: Double = 0.0

      search {
        config.branching match {
          case "top-zero"        => binaryStaticIdx(X, i => X(i).min)
          case "top-one"         => binaryStaticIdx(X, i => X(i).max)
          case "derivative-zero" => binaryIdx(X.toArray, i => -wbddC.derivatives(i), i => X(i).min)
          case "derivative-one"  => binaryIdx(X.toArray, i => wbddC.derivatives(i), i => X(i).max)
          case "bottom-zero"     => binaryStaticIdx(X.reverse, i => X(i).min)
          case "bottom-one"      => binaryStaticIdx(X.reverse, i => X(i).max)
        }
      } onSolution {
        objVal = wbddC.totalValue_
        for (i <- 0 until numberOfMaxVars)
          solution(i) = X(i).value
        if (config.verbose)
          printSolution
        wbddC.setBound(objVal)
      }

      val stats = start()
      println("*" * 10 + " FINISHED " + "*" * 10)
      println("number of failures: " + stats.nFails)
      println("number of nodes: " + stats.nNodes)
      println("number of solutions: " + stats.nSols)
      println("time: " + stats.time)

      println("number of calls to the WbddC propagator: " + wbddC.numberOfCalls_)
      println(f"objective value: $objVal")
      print("solution: ")
      printSolution

    case None =>
  }

  def printSolution(): Unit = {
    for (i <- 0 until numberOfMaxVars)
      print(varMap(i) + ":" + solution(i) + " ")
    println
  }

  def argsParser(): scopt.OptionParser[Config] = {
    new scopt.OptionParser[Config]("MaxExp") {
      head("MaxExp", "1.0")

      opt[File]("bdd-file") required () valueName ("<file>") action { (x, c) => c.copy(bddFile = x)
      } validate { x =>
        if (x.exists()) success else failure("<BDD File> does not exist")
      } text ("the input BDD file")

      opt[Int]("max-card") required () valueName ("<k>") action { (x, c) =>
        c.copy(maxcard = x)
      } validate { x =>
        if (x >= 0) success else failure("Value <maxcard> must be >= 0")
      } text ("the maximum cardinality (positive integer)")

      opt[String]("branching") optional () valueName ("<heuristic>") action { (x, c) =>
        c.copy(branching = x)
      } validate { x =>
        if (List("top-zero", "top-one",
          "derivative-zero", "derivative-one",
          "bottom-zero", "bottom-one") contains x) success else failure("unknown <heuristic>")
      } text ("variable/value selection heuristic [top-zero, top-one, derivative-zero, derivative-one, bottom-zero, bottom-one], default: top-zero")

      opt[Unit]("verbose") abbr ("v") action { (_, c) =>
        c.copy(verbose = true)
      } text ("output all result with every details")

      help("help") text ("Usage")

      override def showUsageOnError = true
    }
  }

}
