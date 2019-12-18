package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.common.NameReceptacle
import akka.http.scaladsl.server.Directives.{complete, get, path, _}
import akka.http.scaladsl.server.{Directive, Route}
import com.leobenkel.vibe.server.Messages.Message

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
