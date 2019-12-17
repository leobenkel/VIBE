lazy val projectName = IO.readLines(new File("PROJECT_NAME")).head
lazy val versionName = IO.readLines(new File("VERSION")).head

lazy val circeVersion = "0.11.1"
lazy val akkaVersion = "2.6.1"
lazy val akkaHttpVersion = "10.1.11"
lazy val slickVersion = "3.3.2"
lazy val zioVersion = "1.0.0-RC17"

lazy val commonSettings = Seq(
  version      := versionName,
  scalaVersion := "2.12.10",
  // https://mvnrepository.com/artifact/org.scalatest/scalatest
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  scalacOptions ++= Seq(
    "-feature",
    "-Yrangepos",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard",
    "-deprecation",
    "-encoding",
    "utf8"
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(server, core)
  .settings(
    publish                       := {},
    publishLocal                  := {},
    Global / onChangedBuildSource := ReloadOnSourceChanges
  )

lazy val core = (project in file("core"))
  .settings(
    name := s"$projectName-core",
    commonSettings,
    libraryDependencies += "dev.zio" %% "zio" % zioVersion withSources ()
  )

lazy val server = (project in file("server"))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    name := s"$projectName-server",
    commonSettings,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion
    ),
    libraryDependencies ++= Seq(
      "dev.zio"               %% "zio"                      % zioVersion withSources (),
      "dev.zio"               %% "zio-macros-core"          % "0.6.0" withSources (),
      "com.typesafe.slick"    %% "slick"                    % slickVersion withSources (),
      "com.typesafe.slick"    %% "slick-codegen"            % slickVersion withSources (),
      "com.github.daddykotex" %% "courier"                  % "2.0.0" withSources (),
      "com.github.pathikrit"  %% "better-files"             % "3.8.0" withSources (),
      "com.typesafe.akka"     %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka"     %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka"     %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka"     %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"        % "logback-classic"           % "1.2.3",
      "com.lihaoyi"           %% "upickle"                  % "0.8.0" withSources (),
      "de.heikoseeberger"     %% "akka-http-upickle"        % "1.29.1" withSources (),
      "com.typesafe.akka"     %% "akka-http-testkit"        % akkaHttpVersion % "it,test",
      "com.typesafe.akka"     %% "akka-actor-testkit-typed" % akkaVersion % "it,test",
      "com.typesafe.akka"     %% "akka-stream-testkit"      % akkaVersion % "it,test",
      "org.scalatest"         %% "scalatest"                % "3.1.0" % "it,test",
      "dev.zio"               %% "zio-test"                 % zioVersion % "it, test",
      "dev.zio"               %% "zio-test-sbt"             % zioVersion % "it, test"
    ),
    testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(core)
