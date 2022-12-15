ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.1"

lazy val root = (project in file("."))
  .settings(
    name := "architecture"
  )

libraryDependencies ++= Seq(
  "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.14" % Test,
  "org.mozilla" % "rhino" % "1.7.14"
)




