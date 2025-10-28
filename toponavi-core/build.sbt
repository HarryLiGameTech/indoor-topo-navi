ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.typelevel" %% "cats-effect" % "3.5.2"
)

lazy val root = (project in file("."))
  .settings(
    name := "toponavi-core"
  )
