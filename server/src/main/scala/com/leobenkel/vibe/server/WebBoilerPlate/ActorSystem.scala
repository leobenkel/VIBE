package com.leobenkel.vibe.server.WebBoilerPlate

// $COVERAGE-OFF$
trait ActorSystem {
  implicit def actorSystem: akka.actor.ActorSystem
}

/**
  * copied from https://github.com/rleibman/full-scala-stack/blob/52805bd99b1c5cd47d6d97df7322c30143451ddb/server/src/main/scala/core/Core.scala#L24
  */
trait BootedActorSystem extends ActorSystem {
  implicit lazy val actorSystem: akka.actor.ActorSystem = akka.actor.ActorSystem("akka-spray")

  /**
    * Ensure that the constructed ActorSystem is shut down when the JVM shuts down
    */
  sys.addShutdownHook({
    actorSystem.terminate()
    ()
  })
}

/**
  * This trait contains the actors that make up our application; it can be mixed in with
  * ``BootedCore`` for running code or ``TestKit`` for unit and integration tests.
  */
trait ActorsSystem { this: ActorSystem =>
}
// $COVERAGE-ON$
