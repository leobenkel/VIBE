package com.leobenkel.vibe.server.WebBoilerPlate

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.scaladsl._
import com.leobenkel.vibe.server.Environment.Config

import scala.concurrent.Future
import scala.util.control.NonFatal

// $COVERAGE-OFF$
trait ServerService extends Config {
  this: RouteServices with ActorsSystem with ActorSystem =>

  val log: LoggingAdapter = Logging.getLogger(actorSystem, this)

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http()
      .bind(
        interface = config.getLocalString("host"),
        port = config.getLocalInt("port")
      )

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource
      .to(Sink.foreach { connection => // foreach materializes the source
        log.debug("Accepted new connection from " + connection.remoteAddress)
        // ... and then actually handle the connection
        try {
          connection.flow.joinMat(routes)(Keep.both).run()
          ()
        } catch {
          case NonFatal(e) =>
            log.error(e, "Could not materialize handling flow for {}", connection)
            throw e
        }
      })
      .run()
}
// $COVERAGE-ON$
