resolvers += Resolver.bintrayRepo("oyvindberg", "ScalajsReactTyped")

addSbtPlugin("org.scalablytyped.japgolly" % "sbt-scalajsreacttyped" % "201912140138")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.31")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.15.0-0.6")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.28" // Needed by sbt-git
libraryDependencies += "org.vafer" % "jdeb" % "1.4" artifacts (Artifact("jdeb", "jar", "jar"))

addSbtPlugin("com.typesafe.sbt"  % "sbt-git"         % "1.0.0")
