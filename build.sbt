name := "Elevator-Coding-Challenge"

version := "0.1"

scalaVersion := "2.12.7"

enablePlugins(PackPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.4" % Test,
  "org.scalatest" %% "scalatest" % "3.1.1" % "test",
  "org.typelevel" %% "cats-core" % "2.1.0",
  "com.propensive" %% "kaleidoscope" % "0.1.0",
  "com.github.scopt" %% "scopt" % "4.0.0-RC2"
)

packMain := Map("ElevatorControlSystem" -> "Main")
