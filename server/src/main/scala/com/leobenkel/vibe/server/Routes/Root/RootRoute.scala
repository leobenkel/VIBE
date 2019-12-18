package com.leobenkel.vibe.server.Routes.Root

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.leobenkel.vibe.server.Messages._
import com.leobenkel.vibe.server.Routes.Utils.RouteUtils._
import com.leobenkel.vibe.server.Routes.Utils.{RouteTrait, RouteTraitWithChild}
import io.circe.generic.auto._

private[Routes] case class RootRoute(root: RouteTraitWithChild) extends RouteTrait {
  lazy final override val url:    String = ""
  lazy final override val method: HttpMethod = HttpMethods.GET

  override def route: Route = {
    pathSingleSlash(routeContent) ~ super.route
  }

  override def methodGetOutput(): Message = {
    MessageWithContent[Seq[RouteDescriptions]](getFullUrl, MessageStatus.Success, "routes") {
      getRoutes(root.getChildRoute)
    }
  }
}
