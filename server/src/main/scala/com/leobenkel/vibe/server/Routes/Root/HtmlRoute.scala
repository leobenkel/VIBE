package com.leobenkel.vibe.server.Routes.Root

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.directives.ContentTypeResolver
import akka.http.scaladsl.server.{Directives, Route}
import com.leobenkel.vibe.server.Messages.MessageSerializer
import com.leobenkel.vibe.server.Routes.Utils.RouteTrait

case class HtmlRoute(pathToStaticAsset: String) extends RouteTrait with Directives {
  lazy final override val url:              String = ""
  lazy final override val method:           HttpMethod = HttpMethods.GET
  override protected def methodGetOutput(): MessageSerializer = ???

  override def getFromDirectory(
    directoryName: String
  )(
    implicit resolver: ContentTypeResolver
  ): Route =
    extractUnmatchedPath { unmatchedPath =>
      getFromFile(s"$pathToStaticAsset/$unmatchedPath")
    }

  lazy final override val route: Route =
    pathEndOrSingleSlash(get(getFromFile(s"$pathToStaticAsset/index.html"))) ~
      get(extractUnmatchedPath { path: Uri.Path =>
        if (ApiRoute.isApiUrl(path)) {
          // if it is API, we want to do like before
          encodeResponse(getFromDirectory(pathToStaticAsset))
        } else {
          // if we know the url doesnt start by the api path,
          // then we are forwarding to the front end
          encodeResponse(getFromDirectory(pathToStaticAsset)) ~
            getFromFile(s"$pathToStaticAsset/index.html")
        }
      })
}
