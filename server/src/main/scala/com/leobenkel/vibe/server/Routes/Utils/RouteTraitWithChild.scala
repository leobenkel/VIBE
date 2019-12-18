package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.server.Directives.{pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import com.leobenkel.vibe.server.Messages._
import com.leobenkel.vibe.server.Routes.Utils.RouteUtils.{RouteDescriptions, getRoutes}
import io.circe.generic.auto._

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
