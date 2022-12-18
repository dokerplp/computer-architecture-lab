ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.1"

lazy val root = (project in file("."))
  .settings (
    name := "architecture",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
  )

libraryDependencies ++= Seq(
  "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.14" % Test,
)

ThisBuild / scalafixDependencies += "io.github.ghostbuster91.scalafix-unified" %% "unified" % "0.0.8"
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"