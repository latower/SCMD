/**
  * ----------------------------------------------------------------------------
  * SCMD : Stochastic Constraint on Monotonic Distributions
  *
  * @author Behrouz Babaki behrouz.babaki@polymtl.ca
  * @author Siegfried Nijssen siegfried.nijssen@uclouvain.be
  * @author Anna Louise Latour a.l.d.latour@liacs.leidenuniv.nl
  *         
  *         Relevant paper: Stochastic Constraint Propagation for Mining 
  *         Probabilistic Networks, IJCAI 2019
  *
  *         Licensed under MIT (https://github.com/latower/SCMD/blob/master/LICENSE_SCMD).
  * ----------------------------------------------------------------------------
  */
  
package propagator

import scala.io.Source

import oscar.cp._
import oscar.algo.Inconsistency
import fim.constraints.CoverSize
import java.io.File

object RunnerMaxExpFim extends App with CPModel {

  def readDB(dbPath: String): (Array[Set[Int]], Int) = {

    val fileLines = Source.fromFile(dbPath).getLines.filter(!_.trim().isEmpty)

    val tdbHorizontal: Array[Array[Int]] = fileLines.map { line => line.trim().mkString.split("\\s+").map(_.toInt).map(bddToCP) }.toArray
    val tdbVertical: Array[Set[Int]] = Array.fill(numberOfMaxVars)(Set[Int]())
    for (i <- tdbHorizontal.indices) {
      for (v <- tdbHorizontal(i)) {
        tdbVertical(v) += i
      }
    }

    val nTrans = tdbHorizontal.length

    (tdbVertical, nTrans)
  }

  private[this] var numberOfMaxVars = -1;
  private[this] var bddToCP: Array[Int] = null;

  val parser = argsParser()
  parser.parse(args, Config()) match {
    case Some(config) =>
      System.err.println("Start MaxExpFim on " + config.bddFile.getAbsolutePath + " and "
        + config.tdbFile.getAbsolutePath)

      val bdd = new Wbdd(config.bddFile.getAbsolutePath)
      numberOfMaxVars = bdd.numberOfMaxVars
      bddToCP = bdd.decMapBddToCP

      val dbPath = config.tdbFile.getAbsolutePath
      val (db, nTrans) = readDB(dbPath)
      val minExp = config.minexp
      val minSup = config.minsup
      var frequency = minSup.toInt
      if (minSup <= 1) frequency = (minSup * nTrans).ceil.toInt

      val X = Array.fill(numberOfMaxVars)(CPBoolVar())
      val solution = Array.fill(numberOfMaxVars)(-1)

      var traceFile = ""
      if (config.collectTraces) {
        traceFile = config.traceFile.getAbsolutePath
        bdd.printSortedIds(traceFile)
      }

      val wbddC = new WbddConstraint(bdd, X, minExp, traceFile)
      val coverage = new CoverSize(X, frequency, numberOfMaxVars, nTrans, db)

      try {
        add(coverage)
        add(wbddC)
      } catch {
        case _: NoSolutionException => { println("infeasible"); System.exit(1) }
      }

      search {
        config.branching match {
          case "top-zero"        => binaryStaticIdx(X, i => X(i).min)
          case "top-one"         => binaryStaticIdx(X, i => X(i).max)
          case "derivative-zero" => binaryIdx(X.toArray, i => -wbddC.derivatives(i), i => X(i).min)
          case "derivative-one"  => binaryIdx(X.toArray, i => wbddC.derivatives(i), i => X(i).max)
          case "item-support" => {
            val Xsorted = (0 until X.size).sortBy(db(_).size).map(X(_)).toArray
            binaryStatic(Xsorted)
          }
          case "bottom-zero" => binaryStaticIdx(X.reverse, i => X(i).min)
          case "bottom-one"  => binaryStaticIdx(X.reverse, i => X(i).max)
        }
      } onSolution {
        if (config.verbose) {
          for (i <- 0 until numberOfMaxVars)
            if (X(i).value == 1)
              print(bdd.decMapCpToBdd(i) + " ")
          println
        }
      }
      val stats = start()
      println("*" * 10 + " FINISHED " + "*" * 10)
      println("number of failures: " + stats.nFails)
      println("number of nodes: " + stats.nNodes)
      println("number of solutions: " + stats.nSols)
      println("time: " + stats.time)

      println("number of calls to the WbddC propagator: " + wbddC.numberOfCalls_)
    case None =>
  }

  def argsParser(): scopt.OptionParser[Config] = {
    new scopt.OptionParser[Config]("MaxExpFim") {
      head("MaxExpFim", "1.0")

      opt[File]("bdd-file") required () valueName ("<file>") action { (x, c) => c.copy(bddFile = x)
      } validate { x =>
        if (x.exists()) success else failure("<BDD File> does not exist")
      } text ("the input BDD file")

      opt[File]("db-file") required () valueName ("<file>") action { (x, c) => c.copy(tdbFile = x)
      } validate { x =>
        if (x.exists()) success else failure("<TDB File> does not exist")
      } text ("the input transaction database")

      opt[Double]("min-exp") required () action { (x, c) =>
        c.copy(minexp = x)
      } validate { x =>
        if (x > 0) success else failure("Value <minexp> must be > 0")
      } text ("the minimum expectation")

      opt[Double]("min-sup") required () action { (x, c) =>
        c.copy(minsup = x)
      } validate { x =>
        if (x > 0) success else failure("Value <minsup> must be > 0")
      } text ("the minimum support - the lower bound of the frequency")

      opt[String]("branching") optional () valueName ("<heuristic>") action { (x, c) =>
        c.copy(branching = x)
      } validate { x =>
        if (List("top-zero", "top-one",
          "derivative-zero", "derivative-one",
          "bottom-zero", "bottom-one",
          "item-support") contains x) success else failure("unknown <heuristic>")
      } text ("variable/value selection heuristic [top-zero, top-one, derivative-zero, derivative-one, bottom-zero, bottom-one], default: top-zero")

      opt[File]("trace-file") optional () valueName ("<file>") action { (x, c) =>
        c.copy(traceFile = x, collectTraces = true)
      } validate { x =>
        if (x.exists() || x.createNewFile()) success else failure("<trace File> can not be created")
      } text ("collect the traces")

      opt[Unit]("verbose") abbr ("v") action { (_, c) =>
        c.copy(verbose = true)
      } text ("output all result with every details")

      help("help") text ("Usage")

      override def showUsageOnError = true
    }
  }

}
