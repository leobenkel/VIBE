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

  lazy private val host: String = config.getLocalString("host")
  lazy private val port: Int = config.getLocalInt("port")

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] = {
    log.info(s"Opening connection at $host:$port")
    Http().bind(interface = host, port = port)
  }

  val bindingFuture: Future[Http.ServerBinding] = serverSource
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
