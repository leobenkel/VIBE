package com.leobenkel.vibe.server.WebBoilerPlate

import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.server.Routes.FullRoutes
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

// $COVERAGE-OFF$
trait RouteServices extends RouteConcatenation { this: ActorsSystem with ActorSystem =>
  protected def routeEnvironment: Any with Database with Console with Clock with Random

  val service: FullRoutes = new FullRoutes {
//    implicit override val dbExecutionContext: ExecutionContext = actorSystem.dispatcher
    lazy final override protected val env: Any with Database with Console with Clock with Random =
      routeEnvironment
  }

  val routes: Route = service.route
}
// $COVERAGE-ON$
