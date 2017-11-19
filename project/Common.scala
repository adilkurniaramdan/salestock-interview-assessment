import sbt.Keys._
import sbt._

object Common {
  val appVersion = "0.0.1"

  val settings: Seq[Def.Setting[_]] = Seq(
    version                                   := appVersion,
    organization                              := "com.adil",
    scalacOptions in Compile                  ++= Seq(
      "-target:jvm-1.8",
      "-encoding",
      "utf8",
      "-feature",                     // Emit warning and location for usages of features that should be imported explicitly.
      "-unchecked",                   // Enable additional warnings where generated code depends on assumptions.
      "-language:implicitConversions",
      "-language:postfixOps",
      "-deprecation"                  // Emit warning and location for usages of deprecated APIs.
    ),
    javacOptions in Compile                   ++= Seq(
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    ),
    javaOptions in run                        ++= Seq(
      "-Xms128m",
      "-Xmx1024m"
    ),
    scalaVersion                              := "2.12.2",
    resolvers                                 ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("staging"),
      Resolver.jcenterRepo,
      "Local Maven"           at Path.userHome.asFile.toURI.toURL + ".m2/repository",
      "Maven repository"      at "http://central.maven.org/maven2/",
      "jitpack.io"            at "https://jitpack.io"
    ),
    sourcesInBase                             := false,
    fork in run                               := false,
    publishArtifact in (Compile, packageDoc)  := false,
    publishArtifact in packageDoc             := false,
    sources         in (Compile, doc)         := Seq.empty,
    parallelExecution in Test                 := false
  )
}