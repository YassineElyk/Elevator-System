name := "Elevator-Coding-Challenge"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.4"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.4" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % "test"
libraryDependencies += "org.typelevel" %% "cats-core" % "2.1.0"
//libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
scalacOptions += "-Ypartial-unification"
