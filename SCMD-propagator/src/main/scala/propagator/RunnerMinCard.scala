package propagator

import oscar.cp._
import oscar.algo.Inconsistency

object RunnerMinCard extends App with CPModel {

  val bdd = new Wbdd(args(0))
  val threshold = args(1).toDouble


  val numberOfMaxVars = bdd.numberOfMaxVars
  val X = Array.fill(numberOfMaxVars)(CPBoolVar())
  val solution = Array.fill(numberOfMaxVars)(-1)

  val wbddC = new WbddConstraintPartial(bdd, X, threshold)

  try {
    minimize(sum(X))
    add(wbddC)
  } catch {
    case _: NoSolutionException => { println("infeasible"); System.exit(1) }
  }

  search {
    binaryStatic(X)
  } onSolution {
    for (i <- 0 until numberOfMaxVars)
      solution(i) = X(i).value
  }

  val stats = start()
  println("\n" + "*" * 10 + " FINISHED " + "*" * 10)
  println("number of failures: " + stats.nFails)
  println("number of nodes: " + stats.nNodes)
  println("number of solutions: " + stats.nSols)
  println("time: " + stats.time)

  println("number of calls to the WbddC propagator: " + wbddC.numberOfCalls_)
  println("objective value: " + solution.reduce(_ + _))
  print("solution: ")
  for (i <- 0 until numberOfMaxVars)
    print(bdd.decMapCpToBdd(i) + ":" + solution(i) + " ")
  println
}
