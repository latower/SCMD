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
  
package scopt

import java.io.File
import java.net.UnknownHostException

private[scopt] object platform {
  val _NL = System.getProperty("line.separator")

  type ParseException = Exception
  def mkParseEx(s: String, p: Int) = new Exception(s"$s at $p")

  trait PlatformReadInstances {
    implicit val fileRead: Read[File] = Read.reads { new File(_) }
  }

  def applyArgumentExHandler[C](desc: String, arg: String): PartialFunction[Throwable, Either[Seq[String], C]] = {
      case e: NumberFormatException => Left(Seq(desc + " expects a number but was given '" + arg + "'"))
      case e: Throwable             => Left(Seq(desc + " failed when given '" + arg + "'. " + e.getMessage))
    }


}

