package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import com.leobenkel.vibe.server.Messages._
import io.circe.generic.auto._
import io.circe.parser.decode

private[Routes] trait RouteTraitWithPost[A, B] extends RouteTrait {
  implicit val da: io.circe.Decoder[A]

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
  ): Either[MessageWithContent[ErrorForJson], MessageWithContent[B]] = {
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

  protected def processCorrectEntity(data: A): MessageWithContent[B]

  protected def methodPostOutput(): Message = {
    error("Missing POST Json entity")
  }

  protected def methodGetOutput(): Message = {
    error("POST method only")
  }
}
