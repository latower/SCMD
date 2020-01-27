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
import java.io.FileOutputStream
import java.io.PrintWriter

import scala.collection.mutable.PriorityQueue
import scala.math.BigInt
import scala.math.abs

import oscar.algo.Inconsistency
import oscar.algo.reversible.ReversibleDouble
import oscar.algo.reversible.ReversibleInt
import oscar.algo.reversible.ReversibleSparseSet
import oscar.cp.core.CPPropagStrength
import oscar.cp.core.Constraint
import oscar.cp.core.variables.CPBoolVar
import oscar.cp.core.variables.CPVar

class WbddConstraintPartial(val bdd: Wbdd, val X: Array[CPBoolVar],
                            val P: Double)
  extends Constraint(X(0).store, "BddWmc") {

  object QueueOrdering extends Enumeration {
    type QueueOrdering = Value
    val Upwards, Downwards = Value
  }
  import QueueOrdering._

  val watch = Array.fill[Boolean](bdd.numberOfNodes)(false)
  
  class UniquePriorityQueue(ord: QueueOrdering) {
    val ord_ = ord match {
      case Upwards   => bdd.UpwardOrdering
      case Downwards => bdd.DownwardOrdering
    }
    val U: PriorityQueue[Int] = PriorityQueue.empty[Int](ord_)

    def isEmpty = U.isEmpty

    def enqueue(entry: Int): Unit = {
      if (entry >= 0 && watch(entry) == false) {
        watch(entry) = true
        U.enqueue(entry)
      }
    }

    def dequeue(): Int = {
      val entry = U.dequeue
      watch(entry) = false
      entry
    }

    def enqueueRelevant(entry: Int): Unit = {
      if (entry >= 0 && relevant(entry) && !watch(entry)) {
        watch(entry) = true
        U.enqueue(entry)
      }
    }

  }
  
  val U = new UniquePriorityQueue(Upwards)
  val Q = new UniquePriorityQueue(Downwards)

  override def associatedVars(): Iterable[CPVar] = X

  var totalValue_ : Double = 0.0
  val derivatives = Array.fill[Double](bdd.numberOfMaxVars)(0)
  var numberOfCalls_ : BigInt = 0

  private[this] var bound = P
  idempotent = true
  private[this] val totalValue: ReversibleDouble = new ReversibleDouble(s, 0)

  private[this] val nodeValues_ = Array.fill[Double](bdd.numberOfNodes)(-1)
  private[this] val freeAndBoundIndices = Array.ofDim[Int](bdd.numberOfMaxVars)
  private[this] val freeVariableOffset: ReversibleInt = new ReversibleInt(s, bdd.numberOfMaxVars)

  private[this] val isRecentlyBound = Array.fill[Boolean](bdd.numberOfMaxVars)(false)

  private[this] val freeIndices: ReversibleSparseSet = new ReversibleSparseSet(s, 0, bdd.numberOfMaxVars - 1)
  private[this] var recentlyBoundIndices = Array.fill[Int](bdd.numberOfMaxVars)(-1)
  private[this] var recentlyBoundIndicesOffset: Int = -1
  private[this] var nodesForFixedVars = Array[Int]()

  private[this] val nodeValues: Array[ReversibleDouble] = Array.ofDim[ReversibleDouble](bdd.numberOfNodes)
  private[this] val pathWeights: Array[ReversibleDouble] = Array.ofDim[ReversibleDouble](bdd.numberOfNodes)
  private[this] val freeIn: Array[ReversibleInt] = Array.ofDim[ReversibleInt](bdd.numberOfNodes)
  private[this] val freeOut: Array[ReversibleInt] = Array.ofDim[ReversibleInt](bdd.numberOfNodes)
  private[this] val reachable: Array[ReversibleInt] = Array.ofDim[ReversibleInt](bdd.numberOfNodes)

  def hiChild(r: Int): Int = bdd.hiNodes(r)
  def loChild(r: Int): Int = bdd.loNodes(r)

  def isHiChildOf(c: Int, p: Int): Boolean = (hiChild(p) == c)
  def isLoChildOf(c: Int, p: Int): Boolean = (loChild(p) == c)

  def getNodePositiveWeight(r: Int): Double = bdd.positiveWeights(bdd.nodeToVar(r))
  def getNodeNegativeWeight(r: Int): Double = bdd.negativeWeights(bdd.nodeToVar(r))

  def activeChild(r: Int, active: Boolean = true): Int = {
    val (_, cpVarIndex) = bdd.getCpVarIndexForBddNode(r)
    val hiChildCondition = (!X(cpVarIndex).isBound || X(cpVarIndex).isTrue)
    val condition = if (active) hiChildCondition else !hiChildCondition
    if (condition)
      hiChild(r)
    else
      loChild(r)
  }

  def removed(p: Int, r: Int): Boolean = {
    val (isDecision, cpVarIndex) = bdd.getCpVarIndexForBddNode(p)
    (isDecision && X(cpVarIndex).isBound && activeChild(p) != r)
  }

  def relevant(r: Int): Boolean = {
    val (isDecision, cpVarIndex) = bdd.getCpVarIndexForBddNode(r)
    (isDecision && !X(cpVarIndex).isBound && reachable(r).getValue > 0) ||
      (freeIn(r).getValue > 0 && freeOut(r).getValue > 0)
  }

  def getNodeValue(nodeId: Int): Double = nodeId match {
    case -1 => 0.0
    case -2 => 1.0
    case _  => nodeValues(nodeId).getValue()
  }

  def getNodeValue_(nodeId: Int): Double = nodeId match {
    case -1 => 0.0
    case -2 => 1.0
    case _  => nodeValues_(nodeId)
  }

  def computeNodeValue(nodeId: Int): Double = {
    var p: Double = getNodeValue_(nodeId)
    if (p < 0) {
      val (isDecision, cpVarIndex) = bdd.getCpVarIndexForBddNode(nodeId)
      if (isDecision) {
        p = computeNodeValue(hiChild(nodeId))
      } else {
        p = getNodeNegativeWeight(nodeId) * computeNodeValue(loChild(nodeId)) +
          getNodePositiveWeight(nodeId) * computeNodeValue(hiChild(nodeId))
      }
      nodeValues_(nodeId) = p
    }
    p
  }

  def initializeValuesAndWeights() = {

    var i: Int = 0
    while (i < bdd.numberOfNodes) {
      computeNodeValue(i)
      i += 1
    }

    val pw = Array.fill[Double](bdd.numberOfNodes)(0)
    for (i <- bdd.roots)
      pw(i) = 1
    for (i <- bdd.sortedNodes) {
      var j: Int = 0
      while (j < bdd.parents(i).length) {
        val parent = bdd.parents(i)(j)
        val w: Double = {
          val (isDecision, cpVar) = bdd.getCpVarIndexForBddNode(parent)
          if (isDecision) {
            if (isHiChildOf(i, parent))
              1.0
            else
              0.0
          } else {
            if (bdd.parent_types(i)(j))
              getNodePositiveWeight(parent)
            else
              getNodeNegativeWeight(parent)
          }
        }
        pw(i) = pw(i) + (pw(parent) * w)
        j += 1
      }
    }

    i = 0
    while (i < bdd.numberOfNodes) {
      nodeValues(i) = new ReversibleDouble(s, nodeValues_(i))
      pathWeights(i) = new ReversibleDouble(s, pw(i))
      i += 1
    }

    totalValue_ = bdd.roots.toList.map(getNodeValue(_)).reduce(_ + _)
    totalValue.setValue(totalValue_)

  }

  def initializeFreeIn(): Unit = {
    val freeIn_ = Array.fill[Int](bdd.numberOfNodes)(0)
    for (i <- bdd.sortedNodes) {
      val isDecision = bdd.isDecisionNode(i)
      if (isDecision || freeIn_(i) != 0) {
        for (j <- List(hiChild(i), loChild(i)).filter(_ >= 0))
          freeIn_(j) += 1
      }
    }

    var i: Int = 0
    while (i < bdd.numberOfNodes) {
      freeIn(i) = new ReversibleInt(s, freeIn_(i))
      i += 1
    }
  }

  def initializeReachable(): Unit = {
    val reachable_ = Array.fill[Int](bdd.numberOfNodes)(0)
    for (r <- bdd.roots)
      reachable_(r) = 1

    for (i <- bdd.sortedNodes)
      for (j <- List(hiChild(i), loChild(i)).filter(_ >= 0))
        reachable_(j) += 1

    var i: Int = 0
    while (i < bdd.numberOfNodes) {
      reachable(i) = new ReversibleInt(s, reachable_(i))
      i += 1
    }
  }

  def initializeFreeOut(): Unit = {
    val freeOut_ = Array.fill[Int](bdd.numberOfNodes)(0)
    for (i <- bdd.sortedNodes.reverse) {
      val isDecision = bdd.isDecisionNode(i)
      if (isDecision || freeOut_(i) != 0)
        for (p <- bdd.parents(i))
          freeOut_(p) += 1
    }

    var i: Int = 0
    while (i < bdd.numberOfNodes) {
      freeOut(i) = new ReversibleInt(s, freeOut_(i))
      i += 1
    }
  }

  override def setup(l: CPPropagStrength): Unit = {
    X.map(_.callPropagateWhenDomainChanges(this))

    var i: Int = 0
    while (i < bdd.numberOfMaxVars) {
      freeAndBoundIndices(i) = i
      i += 1
    }

    initializeValuesAndWeights()
    initializeFreeIn()
    initializeReachable()
    initializeFreeOut()
    propagate()
  }

  def computeDerivatives(): Unit = {
    var i: Int = 0
    while (i < bdd.numberOfMaxVars) {
      derivatives(i) = 0
      i += 1
    }

    var cpVar: Int = 0
    while (cpVar < bdd.numberOfMaxVars) {
      if (!(X(cpVar).isBound))
        for (node <- bdd.getBddNodesForCpVarIndex(cpVar))
          if (reachable(node).getValue > 0)
            derivatives(cpVar) = derivatives(cpVar) +
              pathWeights(node).getValue() * (getNodeValue(loChild(node)) - getNodeValue(hiChild(node)))

      cpVar += 1
    }
  }

  def updateRecentFixes(): Int = {

    var i: Int = 0
    var k: Int = freeVariableOffset.getValue
    recentlyBoundIndicesOffset = 0;

    while (i < k) {
      if (X(freeAndBoundIndices(i)).isBound) {
        freeIndices.removeValue(freeAndBoundIndices(i))
        val temp = freeAndBoundIndices(i)
        freeAndBoundIndices(i) = freeAndBoundIndices(k - 1)
        freeAndBoundIndices(k - 1) = temp
        k -= 1
        recentlyBoundIndices(recentlyBoundIndicesOffset) = temp
        recentlyBoundIndicesOffset += 1
      } else
        i += 1
    }

    nodesForFixedVars = recentlyBoundIndices.take(recentlyBoundIndicesOffset).map(bdd.getBddNodesForCpVarIndex).flatten

    k
  }

  def updateNodeValues(): Double = {
    var d: Double = 0.0
    var newValue: Double = 0.0

    // Only add reachable decision nodes labelled with variables that have just
    // been set to False to the queue
    var j: Int = 0
    while (j < recentlyBoundIndicesOffset) {
      val i = recentlyBoundIndices(j)
      if (X(i).isFalse) {
        for (r <- bdd.getBddNodesForCpVarIndex(i)) {
          if (reachable(r).getValue > 0) {
            U.enqueue(r)
          }
        }
      }
      j += 1
    }
  
    while (!U.isEmpty) {
      val r = U.dequeue
      val oldValue = getNodeValue(r)
      val (isDecision, cpVar) = bdd.getCpVarIndexForBddNode(r)
      if (isDecision) {
        newValue = getNodeValue(activeChild(r))
        if (isRecentlyBound(cpVar) && X(cpVar).isFalse)
          d += pathWeights(r).getValue * (newValue - getNodeValue(hiChild(r)))
      } else {
        newValue = getNodePositiveWeight(r) * getNodeValue(hiChild(r)) +
          getNodeNegativeWeight(r) * getNodeValue(loChild(r))
      }
      // New value can only be lower. If it is indeed lower, propagate this
      // change upwards
      if (newValue < oldValue) {
        nodeValues(r).setValue(newValue)
        for (p <- bdd.parents(r)) {
          if (!removed(p, r)) {
              U.enqueueRelevant(p)
            }
          }
        }
      }
    d
  }

  def updatePathWeights(): Unit = {
    var j: Int = 0
    while (j < recentlyBoundIndicesOffset) {
      val i = recentlyBoundIndices(j)
      if (X(i).isFalse) {
        for (r <- bdd.getBddNodesForCpVarIndex(i)) {
          if (reachable(r).getValue > 0) {
            Q.enqueue(hiChild(r))
            Q.enqueue(loChild(r))
          }
        }
      }
      j += 1
    }

    while (!Q.isEmpty) {
      val r = Q.dequeue
      val oldValue = pathWeights(r).getValue
      var newValue = 0.0
      if (bdd.roots.contains(r))
        newValue = 1.0

      for (p <- bdd.parents(r)) {
        val w = {
          val (isDecision, cpVar) = bdd.getCpVarIndexForBddNode(p)
          if (isDecision) {
            if (activeChild(p) == r) 1.0 else 0.0
          } else {
            if (isHiChildOf(r, p))
              getNodePositiveWeight(p)
            else
              getNodeNegativeWeight(p)
          }
        }
        newValue += pathWeights(p) * w
      }

      if (newValue != oldValue) {
        pathWeights(r).setValue(newValue)
        val (isDecision, cpVar) = bdd.getCpVarIndexForBddNode(r)
        if (!isDecision) {
          Q.enqueueRelevant(hiChild(r))
          Q.enqueueRelevant(loChild(r))
        } else {
          Q.enqueueRelevant(activeChild(r))
        }
      }
    }
  }

  def updateFreeOut(): Unit = {
    val S = nodesForFixedVars.filter(n => reachable(n).getValue > 0 && freeIn(n).getValue > 0)
    
    def updateAndEqueueParents(r: Int): Unit = {
      for (p <- bdd.parents(r)) {
        if (!removed(p, r) && relevant(p)) {
          freeOut(p).decr
          // Only enqueue parents if their FreeOut counter is now 0
          if (freeOut(p).getValue == 0) {
            val (isDecision, cpVarIndex) = bdd.getCpVarIndexForBddNode(p)
            if (!(isDecision && isRecentlyBound(cpVarIndex))) {
              U.enqueue(p)
            }
          }
        }
      }
    }
    
    // First, loop over the nodes whose decision variables have just been fixed
    // to determine which parents of those nodes need to be enqueued to be
    // updated
    for (r <- S) {
      // If the FreeOut counter of this decision node was 0 to begin with, its
      // parents' counters will have to be decreased
      if (freeOut(r).getValue == 0) {
        updateAndEqueueParents(r)
      } 
      // Otherwise, we check if the new situation requires us to update the
      // parents:
      else {
        val c = activeChild(r)
        // If the active child is not a leaf, we check what the new value of the
        // FreeOut counter is, and add parents if necessary
        if (c >= 0) {
          val (childIsDecision, childCpVarIndex) = bdd.getCpVarIndexForBddNode(c)
          if (freeOut(c).getValue > 0 || (childIsDecision && !X(childCpVarIndex).isBound)) {
            freeOut(r).setValue(1)
          } else {
            freeOut(r).setValue(0)
            updateAndEqueueParents(r)
          }
        } 
        // If the active child is a leaf, the current node has a FreeOut counter
        // of 0, and we know that we must update the parent.
        else {
          freeOut(r).setValue(0)
          updateAndEqueueParents(r)
        }
      }
    }

    while (!U.isEmpty) {
      val r = U.dequeue
      val (isDecision, cpVarIndex) = bdd.getCpVarIndexForBddNode(r)
      if (!isDecision || X(cpVarIndex).isBound) {
        updateAndEqueueParents(r)
      }
    }
  }

  def updateReachableFreeIn(): Unit = {

    def enqueueIfProp(r: Int): Unit = {
      if (freeOut(r).getValue > 0 &&
        (freeIn(r).getValue == 0 || reachable(r).getValue == 0))
        Q.enqueue(r)
    }

    // get the nodes labelled with the variables that have recently been fixed,
    // and which are still reachable and lead to nodes labelled with free
    // decision variables
    val S = nodesForFixedVars.filter(n => reachable(n).getValue > 0 && freeOut(n).getValue > 0)

    // for those nodes, update the values of the children and add those 
    // children to the queue if necessary
    for (r <- S) {
      val a = activeChild(r)
      val i = activeChild(r, false)

      // active child
      if (a >= 0 && freeIn(r).getValue == 0) {
        freeIn(a).decr
        val (isDecision, cpVarIndex) = bdd.getCpVarIndexForBddNode(a)
        if (!(isDecision && isRecentlyBound(cpVarIndex))) {
//         if (!S.contains(a)) {
          enqueueIfProp(a)
        }
      }
      // inactive child
      if (i >= 0) {
        freeIn(i).decr
        reachable(i).decr
        val (isDecision, cpVarIndex) = bdd.getCpVarIndexForBddNode(i)
        if (!(isDecision && isRecentlyBound(cpVarIndex))) {
//         if (!S.contains(i)) {
          enqueueIfProp(i)
        }
      }
    }

    while (!Q.isEmpty) {
      val r = Q.dequeue     // new root
      
      // if this root is no longer reachable, update the counters of the children 
      // (if the arc from root to child is not removed)
      if (reachable(r).getValue == 0) {   
        for (c <- List(loChild(r), hiChild(r)).filter(_ >= 0))
          if (!removed(r, c)) {
            freeIn(c).decr
            reachable(c).decr
            enqueueIfProp(c)
          }
      } 
      // else, if current root r is reachable
      else {
        val (isDecision, cpVarIndex) = bdd.getCpVarIndexForBddNode(r)
        val isFreeVar = isDecision && !(X(cpVarIndex).isBound)
        if (freeIn(r).getValue == 0 && !isFreeVar)
          for (c <- List(loChild(r), hiChild(r)).filter(_ >= 0))
            if (!removed(r, c)) {
              freeIn(c).decr
              enqueueIfProp(c)
            }
      }
    }

  }

  def setBound(p: Double): Unit = { bound = p }

  def AlmostEqualRelativeAndAbs(a: Double, b: Double,
                                maxDiff: Double = 0.00000000001): Boolean = {

    var diff: Double = abs(a - b)
    if (diff <= maxDiff)
      return true

    return false
  }

  override def propagate(): Unit = {
    numberOfCalls_ = numberOfCalls_ + 1
    totalValue_ = totalValue.getValue

    val k0: Int = freeVariableOffset.getValue
    val k1 = updateRecentFixes
    if (k1 == k0)
      return // No variable has been initialized since the last call

    var i = 0
//     while (i < X.length) {
//       isRecentlyBound(i) = false
//       i += 1
//     }

    i = 0
    while (i < recentlyBoundIndicesOffset) {
      isRecentlyBound(recentlyBoundIndices(i)) = true
      i += 1
    }

    val totalChanges = updateNodeValues
    totalValue_ += totalChanges

    if (totalValue_ <= bound)
      throw Inconsistency

    updatePathWeights
    computeDerivatives
    
    for (v_idx <- freeIndices.iterator) {
      if (totalValue_ + derivatives(v_idx) <= bound) {
        X(v_idx).assignTrue
        freeIndices.removeValue(v_idx)
      }
    }
    
//     i = 0
//     while (i < bdd.numberOfMaxVars) {
//       if (!X(i).isBound && (totalValue_ + derivatives(i) <= bound)) {
//         X(i).assignTrue
//       }
//       i += 1
//     }
    totalValue.setValue(totalValue_)

    val k2 = updateRecentFixes
    updateReachableFreeIn
    updateFreeOut
    freeVariableOffset.setValue(k2)
    
    i = 0
    while (i < recentlyBoundIndicesOffset) {
      isRecentlyBound(recentlyBoundIndices(i)) = false
      i += 1
    }
    
  }
}
