/**
  * ----------------------------------------------------------------------------
  * SCMD : Stochastic Constraint on Monotonic Distributions
  *
  * @author Behrouz Babaki behrouz.babaki@polymtl.ca
  * @author Siegfried Nijssen siegfried.nijssen@uclouvain.be
  * @author Anna Louise Latour a.l.d.latour@liacs.leidenuniv.nl
  * @version 1.2 (17 December 2019)
  *         
  *         Relevant paper: Stochastic Constraint Propagation for Mining 
  *         Probabilistic Networks, IJCAI 2019
  *
  *         Licensed under MIT (https://github.com/latower/SCMD/blob/master/LICENSE_SCMD).
  * ----------------------------------------------------------------------------
  */
  
package propagator

import java.io.File

case class Config(
  typeOfProblem:    String  = "",
  tdbFile:          File    = new File("."),
  bddFile:          File    = new File("."),
  minsup:           Double  = 0.0,
  minexp:           Double  = 0.0,
  maxcard:          Int     = 0,
  verbose:          Boolean = false,
  branching:        String  = "top-zero") 

object Run extends App {
  try {
    val choice = args(0)
    val arguments = args.drop(1)
    choice match {
      case "ME-F"  => RunnerMaxExpFull.main(arguments)
      case "ME-P"  => RunnerMaxExpPartial.main(arguments)
//       case "MC-P"  => RunnerMinCard.main(arguments)
//       case "FIM" => RunnerMaxExpFim.main(arguments)
      case _     => printUsage
    }
  } catch {
    case _: java.lang.ArrayIndexOutOfBoundsException => printUsage
  }

  def printUsage(): Unit = { println("Usage: Run <ME-F/ME-P/FIM> ARGUMENTS") }
}
