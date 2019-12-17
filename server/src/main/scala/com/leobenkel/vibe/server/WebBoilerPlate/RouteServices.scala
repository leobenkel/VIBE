package com.leobenkel.vibe.server.WebBoilerPlate

import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.leobenkel.vibe.server.Routes.FullRoutes

// $COVERAGE-OFF$
trait RouteServices extends RouteConcatenation { this: ActorsSystem with ActorSystem =>

  val service: FullRoutes = new FullRoutes {
//    implicit override val dbExecutionContext: ExecutionContext = actorSystem.dispatcher
  }

  val routes: Route = service.route
}
// $COVERAGE-ON$
