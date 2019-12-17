package propagator

import java.io.File

case class Config(
  typeOfProblem:    String  = "",
  tdbFile:          File    = new File("."),
  bddFile:          File    = new File("."),
  minsup:           Double  = 0.0,
  minexp:           Double  = 0.0,
  maxcard:          Int     = 0,
  collectTraces:    Boolean = false,
  traceStart:       Int     = -1,
  traceEnd:         Int     = -1,
  verbose:          Boolean = false,
  verify:           Boolean = false,
  traceFile:        File    = new File("."),
  branching:        String  = "top-zero",
  typeOfPropagator: String  = "improved") 
