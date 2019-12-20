package com.leobenkel.vibe.server.Routes.Root

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.server.directives.ContentTypeResolver
import akka.http.scaladsl.server.{Directives, Route}
import better.files.File
import com.leobenkel.vibe.server.Messages.Message
import com.leobenkel.vibe.server.Routes.Utils.RouteTrait

case class HtmlRoute(pathToStaticAsset: String) extends RouteTrait with Directives {
  lazy final override val url:              String = ""
  lazy final override val method:           HttpMethod = HttpMethods.GET
  override protected def methodGetOutput(): Message = ???

  lazy private val dir: File = File(pathToStaticAsset)

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
      get(extractUnmatchedPath(path => encodeResponse(getFromDirectory(pathToStaticAsset))))
}
