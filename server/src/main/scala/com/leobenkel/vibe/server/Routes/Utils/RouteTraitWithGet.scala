package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.common.NameReceptacle
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.server.Directives.{complete, get, path, _}
import akka.http.scaladsl.server.{Directive, Route}
import com.leobenkel.vibe.server.Messages.MessageSerializer

private[Routes] trait RouteTraitWithGet[A] extends RouteTrait {
  lazy final override val method: HttpMethod = HttpMethods.GET

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

  protected def methodGetOutput(methodGetParameters: A): MessageSerializer

  protected def methodGetOutput(): MessageSerializer = {
    errorWithContent[Seq[String]](
      errorMessage = "Missing GET parameters",
      contentFieldName = "missingGetParameters"
    ) {
      methodGetParameters().map(_.name)
    }
  }
}
