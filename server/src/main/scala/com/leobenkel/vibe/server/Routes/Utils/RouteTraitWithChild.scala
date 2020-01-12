package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.server.Directives.{pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import com.leobenkel.vibe.core.Messages.MessageStatus
import com.leobenkel.vibe.server.Messages.MessageSerializer
import com.leobenkel.vibe.server.Messages.ToMessage.RichMessage
import com.leobenkel.vibe.server.Routes.Utils.RouteUtils.{RouteDescriptions, getRoutes}
import io.circe.generic.auto._

private[Routes] trait RouteTraitWithChild extends RouteTrait {
  lazy final override val method: HttpMethod = HttpMethods.GET

  override def route: Route = {
    ignoreTrailingSlash {
      checkHierarchy()

      pathPrefix(url) {
        pathEnd(routeContent) ~ getChildRoute.map(_.route).reduce(_ ~ _)
      }
    }
  }

  private[Routes] def getChildRoute: Seq[RouteTrait]

  override protected def methodGetOutput(): MessageSerializer = {
    RichMessage[Seq[RouteDescriptions]](getFullUrl, MessageStatus.Success, "routes") {
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
