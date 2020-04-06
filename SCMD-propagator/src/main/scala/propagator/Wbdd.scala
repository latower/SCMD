/**
  * ----------------------------------------------------------------------------
  * SCMD : Stochastic Constraint on Monotonic Distributions
  *
  * @author Behrouz Babaki behrouz.babaki@polymtl.ca
  * @author Siegfried Nijssen siegfried.nijssen@uclouvain.be
  * @author Anna Louise Latour a.l.d.latour@liacs.leidenuniv.nl
  * @version 1.3 (6 April 2020)
  *         
  *         Relevant paper: Stochastic Constraint Propagation for Mining 
  *         Probabilistic Networks, IJCAI 2019
  *
  *         Licensed under MIT (https://github.com/latower/SCMD/blob/master/LICENSE_SCMD).
  * ----------------------------------------------------------------------------
  */

package propagator

import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.MutableList
import java.io.PrintWriter
import java.io.FileOutputStream
import java.io.File

class Wbdd(bddFileName: String) {

  private[this] val lines: List[String] = Source.fromFile(bddFileName).getLines().toList
  private[this] val nv: Array[Int] = lines(0).trim().split(" ").map(_.toInt)
  val numberOfNodes: Int = nv(0)
  private[this] val numberOfVariables: Int = nv(1)
  val roots: Set[Int] = lines(1).split(" ").map(_.toInt).toSet
  private[this] val nodeInfo: List[List[Int]] = lines.slice(2, numberOfNodes + 2).map(_.trim()).map(_.split(" ").toList.map(_.toInt)).toList

  val nodeToVar: Array[Int] = nodeInfo.map(_.head).toArray
  val loNodes: Array[Int] = nodeInfo.map(_(1)).toArray
  val hiNodes: Array[Int] = nodeInfo.map(_(2)).toArray

  private[this] val varToNodes_ = Array.fill(numberOfVariables)(ArrayBuffer[Int]())
  (0 until numberOfNodes).foreach(i => varToNodes_(nodeToVar(i)).append(i))
  val varToNodes: Array[Array[Int]] = varToNodes_.map(_.toArray)

  private[this] val parents_ = Array.fill(numberOfNodes)(ArrayBuffer[Int]())
  private[this] val parent_types_ = Array.fill(numberOfNodes)(ArrayBuffer[Boolean]())

  for (u <- 0 until numberOfNodes) {
    if (loNodes(u) >= 0) {
      parents_(loNodes(u)).append(u)
      parent_types_(loNodes(u)).append(false)
    }
    if (hiNodes(u) >= 0) {
      parents_(hiNodes(u)).append(u)
      parent_types_(hiNodes(u)).append(true)
    }
  }

  val parents: Array[Array[Int]] = parents_.map(_.toArray)
  val parent_types: Array[Array[Boolean]] = parent_types_.map(_.toArray)

  private[this] val weights = lines(numberOfNodes + 2).split(" ").map(_.toDouble).toList
  val positiveWeights: Array[Double] = weights.zipWithIndex.filter(_._2 % 2 == 0).map(_._1).toArray
  val negativeWeights: Array[Double] = weights.zipWithIndex.filter(_._2 % 2 == 1).map(_._1).toArray
  val maxVars: Array[Int] = lines(numberOfNodes + 3).split(" ").map(_.toInt)
  val numberOfMaxVars: Int = maxVars.length

  val decMapBddToCP: Array[Int] = Array.fill(numberOfVariables)(-1)
  val decMapCpToBdd: Array[Int] = Array.ofDim[Int](numberOfMaxVars)
  for (i <- 0 until numberOfMaxVars) {
    decMapBddToCP(maxVars(i)) = i
    decMapCpToBdd(i) = maxVars(i)
  }

  def topologicalSort(): Array[Int] = {
    val visited = Array.fill[Boolean](numberOfNodes)(false)
    val collectedNodes = MutableList[Int]()
    def visit(n: Int): Unit = {
      if (!visited(n)) {
        visited(n) = true
        if (hiNodes(n) >= 0)
          visit(hiNodes(n))
        if (loNodes(n) >= 0)
          visit(loNodes(n))
        collectedNodes += n
      }
    }
    (0 until numberOfNodes).foreach(visit(_));
    collectedNodes.reverse.toArray
  }

  val sortedNodes: Array[Int] = topologicalSort()
  val positionInSorted: Array[Int] = Array.ofDim[Int](numberOfNodes)
  for (i <- 0 until numberOfNodes)
    positionInSorted(sortedNodes(i)) = i

  object UpwardOrdering extends Ordering[Int] {
    def compare(a: Int, b: Int) = positionInSorted(a) compare positionInSorted(b)
  }

  object DownwardOrdering extends Ordering[Int] {
    def compare(a: Int, b: Int) = UpwardOrdering.compare(b, a)
  }

  def getCpVarIndexForBddNode(nodeId: Int): (Boolean, Int) = {
    val cpVar = decMapBddToCP(nodeToVar(nodeId))
    val isDecision = cpVar match {
      case -1 => false
      case _  => true
    }
    (isDecision, cpVar)
  }

  val isDecisionNode: Array[Boolean] = (0 until numberOfNodes).map(getCpVarIndexForBddNode(_)._1).toArray
  def getBddNodesForCpVarIndex(cpVar: Int): Array[Int] = varToNodes(decMapCpToBdd(cpVar))

  def print() = {
    println("number of nodes: " + numberOfNodes)
    println("number of variables: " + numberOfVariables)
    println("number of max variables: " + numberOfMaxVars)
    println("roots: " + roots.toArray.deep.mkString(" "))
    println("max variables: " + maxVars.deep.mkString(" "))
    println("positive weights: " + positiveWeights.deep.mkString(" "))
    println("negative weights: " + negativeWeights.deep.mkString(" "))
    println("bdd to cp: " + decMapBddToCP.deep.mkString(" "))
    println("cp to bdd: " + decMapCpToBdd.deep.mkString(" "))
    println("node to var: " + nodeToVar.deep.mkString(" "))
    println("var to node: " + varToNodes.deep.mkString(" "))
    println("\nhi nodes: " + hiNodes.deep.mkString(" "))
    println("lo nodes: " + loNodes.deep.mkString(" "))
    println("parents: " + parents.deep.mkString(" "))
    println("parent types: " + parent_types.deep.mkString(" "))
    println("sorted nodes: " + sortedNodes.deep.mkString(" "))
  }
}


