/**
	* ----------------------------------------------------------------------------
	* SCMD : Stochastic Constraint on Monotonic Distributions
	*
	* @author Behrouz Babaki behrouz.babaki@polymtl.ca
	* @author Siegfried Nijssen siegfried.nijssen@uclouvain.be
	* @author Anna Louise Latour a.l.d.latour@liacs.leidenuniv.nl
    * @author Daniël Fokkinga
    * @version 1.3 (6 April 2020)
	*				 
	*				 Relevant paper: Stochastic Constraint Propagation for Mining 
	*				 Probabilistic Networks, IJCAI 2019
	*
	*				 Licensed under MIT (https://github.com/latower/SCMD/blob/master/LICENSE_SCMD).
	* ----------------------------------------------------------------------------
	*/
	
package propagator

import oscar.cp._
import oscar.algo.Inconsistency
import java.io.File
import scala.io.Source

object RunnerMaxExpPartial extends App with CPModel {

	private[this] var numberOfMaxVars = -1;
	private[this] var solution: Array[Int] = null;
	private[this] var varMap: Array[Int] = null;

	val parser = argsParser()
	parser.parse(args, Config()) match {
		case Some(config) =>
			System.err.println("Start MaxExpPartial on " + config.bddFile.getAbsolutePath)

			val bdd = new Wbdd(config.bddFile.getAbsolutePath)
			val card = config.maxcard

			numberOfMaxVars = bdd.numberOfMaxVars
			varMap = bdd.decMapCpToBdd

			val X = Array.fill(numberOfMaxVars)(CPBoolVar())
			solution = Array.fill(numberOfMaxVars)(-1);

			val wbddC = new WbddConstraintPartial(bdd, X, 0.0)
			
			val heuristics = new Array[Float](numberOfMaxVars)
			if (!(config.branching == "top-zero") 
				&& !(config.branching == "top-one") 
				&& !(config.branching == "bottom-zero")
				&& !(config.branching == "bottom-one")
				&& !(config.branching == "derivative-zero")
				&& !(config.branching == "derivative-one")
				&& !(config.branching == "binary-last-conflict")
				&& !(config.branching == "conflict-ordering-search")
				){
				val lines: List[String] = Source.fromFile(config.heuristicFile).getLines().toList
				val temp = lines.filterNot(_.isEmpty).map{line=>(line)}.toArray 
				for (i <- 0 until numberOfMaxVars)
					heuristics(i) = temp(i).toFloat
			}

			try {
				add(sum(X) <= card)
				add(wbddC)
			} catch {
				case _: NoSolutionException => { println("infeasible"); System.exit(1) }
			}

			var objVal: Double = 0.0

			search {
				config.branching match {
					case "top-zero"				=> binaryStaticIdx(X, i => X(i).min)
					case "top-one"				 => binaryStaticIdx(X, i => X(i).max)
					case "derivative-zero" => binaryIdx(X.toArray, i => -wbddC.derivatives(i), i => X(i).min)
					case "derivative-one"	=> binaryIdx(X.toArray, i => wbddC.derivatives(i), i => X(i).max)
					case "bottom-zero"		 => binaryStaticIdx(X.reverse, i => X(i).min)
					case "bottom-one"			=> binaryStaticIdx(X.reverse, i => X(i).max)
					case "degree-zero"		=> binaryIdx(X.toArray, i => heuristics(i), i => X(i).min)
					case "degree-one"		=> binaryIdx(X.toArray, i => -heuristics(i), i => X(i).max)
					case "influence-zero"	=> binaryIdx(X.toArray, i => heuristics(i), i => X(i).min)
					case "influence-one"	=> binaryIdx(X.toArray, i => -heuristics(i), i => X(i).max)
					case "betweenness-zero"	=> binaryIdx(X.toArray, i => heuristics(i), i => X(i).min)
					case "betweenness-one"	=> binaryIdx(X.toArray, i => -heuristics(i), i => X(i).max)
					case "local-sim-zero"	=> binaryIdx(X.toArray, i => heuristics(i), i => X(i).min)
					case "local-sim-one"	=> binaryIdx(X.toArray, i => -heuristics(i), i => X(i).max)
					case "quadrangle-zero"	=> binaryIdx(X.toArray, i => heuristics(i), i => X(i).min)
					case "quadrangle-one" => binaryIdx(X.toArray, i => -heuristics(i), i => X(i).max)
					case "triangle-zero"	=> binaryIdx(X.toArray, i => heuristics(i), i => X(i).min)
					case "triangle-one"	=> binaryIdx(X.toArray, i => -heuristics(i), i => X(i).max)
					case "forest-fire-zero"	=> binaryIdx(X.toArray, i => heuristics(i), i => X(i).min)
					case "forest-fire-one"	=> binaryIdx(X.toArray, i => -heuristics(i), i => X(i).max)
					case "random-zero"	=> binaryIdx(X.toArray, i => heuristics(i), i => X(i).min)
					case "random-one"		=> binaryIdx(X.toArray, i => -heuristics(i), i => X(i).max)
					case "binary-last-conflict"      => binaryLastConflict(X.toArray)
					case "conflict-ordering-search"  => conflictOrderingSearch(X.toArray, i => wbddC.derivatives(i), i => X(i).max)
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
				if ((List("top-zero", "top-one",
				"derivative-zero", "derivative-one",
				"bottom-zero", "bottom-one",
				"item-support", "fixed",
				"degree-zero", "degree-one",
				"influence-zero", "influence-one",
				"betweenness-zero", "betweenness-one",
				"local-sim-zero", "local-sim-one",
				"quadrangle-zero", "quadrangle-one",
				"triangle-zero", "triangle-one",
				"forest-fire-zero", "forest-fire-one",
				"random-zero", "random-one",
				"binary-last-conflict", "conflict-ordering-search")) contains x) success else failure("unknown <strategy>")
			} text ("variable/value selection heuristic [many options], default: ?")
			
			opt[Unit]("verbose") abbr ("v") action { (_, c) =>
				c.copy(verbose = true)
			} text ("output all result with every details")
			
			opt[File]("heuristic-file") optional () valueName ("<file>") action { (x, c) => c.copy(heuristicFile = x)
			} validate { x =>
				if (x.exists()) success else failure("<Heuristic File> does not exist")
			} text ("the file with external heuristic")

			help("help") text ("Usage")

			override def showUsageOnError = true
		}
	}

}
