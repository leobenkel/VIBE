package com.leobenkel.vibe.server.Routes.Root

import com.leobenkel.vibe.server.Routes.Utils.{RouteTrait, RouteTraitWithChild}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.leobenkel.vibe.server.Messages.{Message, MessageStatus, MessageWithContent}
import io.circe.generic.auto._
import com.leobenkel.vibe.server.Routes.Utils.RouteUtils._


private[Routes] case class RootRoute(root: RouteTraitWithChild) extends RouteTrait {
  override val url: String = ""

  override def route: Route = {
    pathSingleSlash(routeContent) ~ super.route
  }

  override def methodGetOutput(): Message = {
    MessageWithContent[Seq[RouteDescriptions]](getFullUrl, MessageStatus.Success, "routes") {
      getRoutes(root.getChildRoute)
    }
  }
}
