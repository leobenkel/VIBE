val projectName = IO.readLines(new File("PROJECT_NAME")).head
val versionName = IO.readLines(new File("VERSION")).head

val circeVersion = "0.11.1"

lazy val commonSettings = Seq(
  version      := versionName,
  scalaVersion := "2.12.10",
  // https://mvnrepository.com/artifact/org.scalatest/scalatest
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

lazy val core = (project in file("core"))
  .settings(
    name := s"$projectName-core",
    commonSettings,
    libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC17"
  )

lazy val server = (project in file("server"))
  .settings(
    name := s"$projectName-server",
    commonSettings,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion
    )
  )
  .dependsOn(core)
