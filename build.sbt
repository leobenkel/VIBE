val projectName = IO.readLines(new File("PROJECT_NAME")).head
val versionName = IO.readLines(new File("VERSION")).head

lazy val commonSettings = Seq(
  name         := projectName,
  version      := versionName,
  scalaVersion := "2.12.10",
  // https://mvnrepository.com/artifact/org.scalatest/scalatest
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC17"
  )
