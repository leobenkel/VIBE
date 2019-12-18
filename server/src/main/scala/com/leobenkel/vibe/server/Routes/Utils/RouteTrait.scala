package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.leobenkel.vibe.server.Messages._
import io.circe.Encoder

import scala.reflect.ClassTag

private[Routes] trait RouteTrait {
  def parent: Option[RouteTraitWithChild] = None
  def url: String
  final def getParent: Option[RouteTraitWithChild] = parent

  final def getFullUrl: String = {
    getParent match {
      case None    => s"/$url"
      case Some(p) => s"${p.getFullUrl}/$url"
    }
  }

  def route: Route = path(url)(routeContent)

  protected def error(errorMessage: String): Message =
    ErrorMessage(getFullUrl)(errorMessage)

  protected def getStatusCode: StatusCode = StatusCodes.OK

  protected def errorWithContent[A: ClassTag](
    errorMessage:     String,
    contentFieldName: String
  )(
    content: => A
  )(
    implicit encoder: Encoder[A]
  ): MessageWithContent[A] = {
    ErrorMessage.withContent[A](getFullUrl)(errorMessage, contentFieldName)(content)
  }

  protected def routeContent: Route = get(complete(getStatusCode, methodGetOutput().toJsonString))

  protected def methodGetOutput(): Message
}
