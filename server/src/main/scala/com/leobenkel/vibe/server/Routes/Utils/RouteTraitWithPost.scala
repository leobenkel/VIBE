package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import com.leobenkel.vibe.core.Messages.ErrorForJson
import com.leobenkel.vibe.server.Messages._
import io.circe.generic.auto._
import io.circe.parser.decode

private[Routes] trait RouteTraitWithPost[A, B] extends RouteTrait {
  implicit val da: io.circe.Decoder[A]
  lazy final override val method: HttpMethod = HttpMethods.POST

  final override def route: Route = {
    path(url) {
      post {
        entity(as[String]) { methodPostParameters =>
          complete(methodPostOutput(methodPostParameters).fold(_.toJsonString, _.toJsonString))
        }
      } ~
        post {
          complete(methodPostOutput().toJsonString)
        }
    } ~ super.route
  }

  final protected def methodPostOutput(
    jsonInput: String
  ): Either[RichMessageSerializer[ErrorForJson], RichMessageSerializer[B]] = {
    decode[A](jsonInput) match {
      case Left(e) =>
        Left(
          errorWithContent[ErrorForJson](
            errorMessage = "Failed to parse Json",
            contentFieldName = "error"
          )(ErrorForJson(e))
        )
      case Right(inputDecoded) =>
        Right(processCorrectEntity(inputDecoded))
    }
  }

  protected def processCorrectEntity(data: A): RichMessageSerializer[B]

  protected def methodPostOutput(): MessageSerializer = {
    error("Missing POST Json entity")
  }

  protected def methodGetOutput(): MessageSerializer = {
    error("POST method only")
  }
}
