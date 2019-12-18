package com.leobenkel.vibe.server

import com.leobenkel.vibe.core.Services.{Database, DatabaseInMemory}
import com.leobenkel.vibe.server.Environment.{Config, LiveEnvironment}
import com.leobenkel.vibe.server.WebBoilerPlate._
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

object ServerApp
    extends App //To run it
    with BootedActorSystem //For stop and start
    with ActorsSystem with RouteServices // The api
    with ServerService // As a web service
    with LiveEnvironment with Config.Live {
//  override implicit val dbExecutionContext: ExecutionContext = actorSystem.dispatcher
  override protected def routeEnvironment: Any with Database with Console with Clock with Random =
    new Database with Console.Live with Clock.Live with Random.Live {
      override def database: Database.Service = DatabaseInMemory
    }
}
