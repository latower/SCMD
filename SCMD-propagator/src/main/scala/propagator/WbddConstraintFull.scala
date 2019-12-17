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

import oscar.cp.`package`.CPModel
import oscar.cp.core.variables.CPIntVar
import oscar.cp.core.Constraint
import oscar.cp.core.CPPropagStrength
import oscar.cp.core.variables.CPVar
import oscar.cp.add
import oscar.cp.core.variables.CPBoolVar
import oscar.algo.Inconsistency
import scala.collection.mutable.PriorityQueue
import scala.collection.mutable.ArrayBuffer
import oscar.algo.reversible.ReversibleContext
import oscar.algo.reversible.ReversibleDouble
import oscar.algo.reversible.ReversibleBoolean
import oscar.algo.reversible.ReversibleInt
import scala.math.BigInt
import java.math.MathContext

class WbddConstraintFull(val bdd: Wbdd, val X: Array[CPBoolVar],
                         val P: Double)
  extends Constraint(X(0).store, "BddWmc") {

  override def associatedVars(): Iterable[CPVar] = X

  var totalValue_ : Double = 0.0
  var numberOfCalls_ : BigInt = 0
  val derivatives = Array.fill[Double](bdd.numberOfMaxVars)(0)
  val pathWeights = Array.fill[Double](bdd.numberOfNodes)(0.0)

  private[this] var bound = P
  idempotent = true

  def getProbability(nodeId: Int): Double = nodeId match {
    case -1 => 0.0
    case -2 => 1.0
    case _  => probabilities(nodeId)
  }

  val probabilities = Array.ofDim[Double](bdd.numberOfNodes)

  def computeProbability(nodeId: Int): Double = {
    val currentProbability = getProbability(nodeId)
    if (currentProbability >= 0) {
      currentProbability
    } else {
      var p: Double = 0.0
      val (isDecision, cpVarIndex) = bdd.getCpVarIndexForBddNode(nodeId)
      if (isDecision) {
        p = X(cpVarIndex) match {
          case i if (!i.isBound || i.isTrue) => computeProbability(bdd.hiNodes(nodeId))
          case _                             => computeProbability(bdd.loNodes(nodeId))
        }
      } else {
        p = bdd.negativeWeights(bdd.nodeToVar(nodeId)) * computeProbability(bdd.loNodes(nodeId)) +
          bdd.positiveWeights(bdd.nodeToVar(nodeId)) * computeProbability(bdd.hiNodes(nodeId))
      }
      probabilities(nodeId) = p
      p
    }
  }

  def computeDerivatives(): Unit = {
    var i: Int = 0
    while (i < bdd.numberOfMaxVars) {
      derivatives(i) = 0
      i += 1
    }
    for (i <- bdd.sortedNodes) {
      if (bdd.roots.contains(i))
        pathWeights(i) = 1.0
      else
        pathWeights(i) = 0.0
      for (j <- 0 until bdd.parents(i).length) {
        val w: Double = {
          val parent = bdd.parents(i)(j)
          val (isDecision, cpVar) = bdd.getCpVarIndexForBddNode(parent)
          if (isDecision) {
            if (bdd.hiNodes(parent) == i && (!X(cpVar).isBound || X(cpVar).isTrue))
              1.0
            else if (bdd.loNodes(parent) == i && X(cpVar).isFalse)
              1.0
            else
              0.0
          } else {
            if (bdd.parent_types(i)(j))
              bdd.positiveWeights(bdd.nodeToVar(bdd.parents(i)(j)))
            else
              bdd.negativeWeights(bdd.nodeToVar(bdd.parents(i)(j)))
          }
        }
        pathWeights(i) = pathWeights(i) + (pathWeights(bdd.parents(i)(j)) * w)
      }
    }

    for (cpVar <- 0 until bdd.numberOfMaxVars)
      for (node <- bdd.getBddNodesForCpVarIndex(cpVar))
        derivatives(cpVar) = derivatives(cpVar) + pathWeights(node) * (computeProbability(bdd.loNodes(node)) - computeProbability(bdd.hiNodes(node)))
  }

  override def setup(l: CPPropagStrength): Unit = {
    X.map(_.callPropagateWhenDomainChanges(this))
    propagate()
  }

  def setBound(p: Double): Unit = { bound = p }

  override def propagate(): Unit = {
    numberOfCalls_ += 1

    for (i <- 0 until bdd.numberOfNodes)
      probabilities(i) = -1.0
    for (i <- 0 until bdd.numberOfNodes)
      computeProbability(i)
    totalValue_ = bdd.roots.toList.map(computeProbability(_)).reduce(_ + _)

    if (totalValue_ <= bound)
      throw Inconsistency

    computeDerivatives

    for (i <- 0 until bdd.numberOfMaxVars)
      if (!X(i).isBound && (totalValue_ + derivatives(i) <= bound))
        X(i).assignTrue

  }

}
