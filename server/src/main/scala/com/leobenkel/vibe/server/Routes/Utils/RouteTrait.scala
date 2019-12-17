package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.common.NameReceptacle
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Route}
import com.leobenkel.vibe.server.Messages._
import com.leobenkel.vibe.server.Routes.Utils.RouteUtils._
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.parser.decode

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

  protected def error(errorMessage: String): Message = {
    ErrorMessage(getFullUrl)(errorMessage)
  }

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

private[Routes] trait RouteTraitWithGet[A] extends RouteTrait {
  final override def route: Route = {
    path(url) {
      get {
        methodGetRetrieveParameters().tapply { methodGetParameters: A =>
          complete(methodGetOutput(methodGetParameters).toJsonString)
        }
      }
    } ~ super.route
  }

  protected def methodGetRetrieveParameters(): Directive[A]

  protected def methodGetParameters(): Seq[NameReceptacle[_]]

  protected def methodGetOutput(methodGetParameters: A): Message

  protected def methodGetOutput(): Message = {
    errorWithContent[Seq[String]](
      errorMessage = "Missing GET parameters",
      contentFieldName = "missingGetParameters"
    ) {
      methodGetParameters().map(_.name)
    }
  }
}

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

private[Routes] trait RouteTraitWithChild extends RouteTrait {

  override def route: Route = {
    checkHierarchy()

    pathPrefix(url) {
      pathEnd(routeContent) ~ getChildRoute.map(_.route).reduce(_ ~ _)
    }
  }

  private[Routes] def getChildRoute: Seq[RouteTrait]

  override protected def methodGetOutput(): Message = {
    MessageWithContent[Seq[RouteDescriptions]](getFullUrl, MessageStatus.Success, "routes") {
      getRoutes(getChildRoute)
    }
  }

  private def checkHierarchy(): Unit = {
    // Set the back hierarchy
    getChildRoute
      .foreach { r =>
        assert(r.getParent.isDefined, s"'${r.getFullUrl}' : the parent was not set.")
        assert(
          r.getParent.get.getFullUrl == this.getFullUrl,
          s"'${r.getFullUrl}' : the parent was '${r.getParent.get.getFullUrl}' " +
            s"instead of '${this.getFullUrl}'."
        )
      }
  }
}
