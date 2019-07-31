// import Dependencies._

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.12.4"
ThisBuild / target := file("SCMDpropagator")

resolvers += Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/releases/"))

resolvers += "Oscar Snapshots" at "http://artifactory.info.ucl.ac.be/artifactory/libs-snapshot-local/"

libraryDependencies += "oscar" %% "oscar-cp" % "4.0.0-SNAPSHOT" withSources()
libraryDependencies += "oscar" %% "oscar-algo" % "4.0.0-SNAPSHOT" withSources()




// lazy val linear = (project in file("RunnerMaxProbLinear"))
//   .settings(
//     name := "linear",
//     libraryDependencies ++= scmdDeps
//   )
//   
// lazy val subLinear = (project in file("RunnerMaxProbSubLinear"))
//   .settings(
//     name := "subLinear",
//     libraryDependencies ++= scmdDeps
//   )

enablePlugins(JavaAppPackaging)

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}
