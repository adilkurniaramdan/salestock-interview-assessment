import play.sbt.PlayImport._
import sbt._

object Dependencies {
  val akkaVersion               = "2.5.4"
  val ficusVersion              = "1.4.3"
  val scalaTestVersion          = "3.0.1"
  val scalaGuiceVersion         = "4.1.0"
  val swaggerWebjarVersion      = "2.2.10-1"

  val akkaDependencies          = Seq(
    "com.typesafe.akka"               %% s"akka-actor"                   % akkaVersion,
    "com.typesafe.akka"               %% s"akka-persistence"             % akkaVersion,
    "com.typesafe.akka"               %% s"akka-testkit"                  % akkaVersion              % "test"
  )

  val testDependencies         = Seq(
    "org.scalatest"                   %% "scalatest"                      % scalaTestVersion        % "test",
    "org.scalatestplus.play"          %% "scalatestplus-play"             % "3.1.2"                 % Test,
    "org.easymock"                  % "easymock"                              % "3.5"               % "test"
  )

  val logback                  =
    "ch.qos.logback"                  %   "logback-classic"               % "1.1.3"

  val utilityDependencies      = Seq(
    "commons-io"                      %  "commons-io"                     % "2.4"                   % "test",
    "com.iheart"                      %% "ficus"                          % ficusVersion,
    "net.codingwell"                  %% "scala-guice"                    % scalaGuiceVersion,
    guice,
    ehcache,
    filters
  )

  val webjarDependencies        = Seq(
    "org.webjars"                     % "swagger-ui"                      % swaggerWebjarVersion
  )

  val h2hWebDependencies       =
    akkaDependencies          ++
    testDependencies          ++
    utilityDependencies       ++
    webjarDependencies        ++
    Seq(logback)

}

