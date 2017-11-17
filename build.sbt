import Dependencies._
import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .settings(Common.settings: _*)
  .settings(
    libraryDependencies     ++= h2hWebDependencies,
    swaggerDomainNameSpaces := Seq("models")
  )




