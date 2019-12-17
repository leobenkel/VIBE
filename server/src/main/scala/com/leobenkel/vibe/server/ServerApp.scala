package com.leobenkel.vibe.server

import com.leobenkel.vibe.server.Environment.{Config, LiveEnvironment}
import com.leobenkel.vibe.server.WebBoilerPlate._

object ServerApp
    extends App //To run it
    with BootedActorSystem //For stop and start
    with ActorsSystem with RouteServices // The api
    with ServerService // As a web service
    with LiveEnvironment with Config.Live {
//  override implicit val dbExecutionContext: ExecutionContext = actorSystem.dispatcher
}
