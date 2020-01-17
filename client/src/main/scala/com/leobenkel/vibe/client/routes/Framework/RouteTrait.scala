package com.leobenkel.vibe.client.routes.Framework

import com.leobenkel.vibe.client.app.Config
import com.leobenkel.vibe.client.components.AbstractComponent
import com.leobenkel.vibe.client.routes.AppRouter.AppPageData
import japgolly.scalajs.react.AsyncCallback
import japgolly.scalajs.react.extra.router.{RouterCtl, _}

trait RouteTrait extends AbstractComponent {
  case object PageData extends AppPageData
  lazy final val selfRoute: this.type = this
  def url:  String
  def name: String

  def route(dsl: RouterConfigDsl[AppPageData]): dsl.Rule = {
    import dsl._
    staticRoute(this.name, this.PageData) ~> renderR(
      (_: RouterCtl[AppPageData]) => engine.apply()
    )
  }

  protected def engine: RouteEngine.Engine[_]
}

object RouteTrait {
  lazy final val GetHostUrl: AsyncCallback[Either[String, String]] = {
    for {
      host <- Config.getKey("host")
      port <- Config.getKey("port")
    } yield {
      for {
        p <- port.flatMap {
          _.asNumber
            .flatMap(_.toInt)
            .fold[Either[String, Int]](Left("Could not covert to 'Int'"))(Right(_))
        }
        h <- host
          .flatMap {
            _.asString
              .fold[Either[String, String]](Left("Could not covert to 'String'"))(Right(_))
          }
      } yield {
        s"$h:$p"
      }
    }
  }
}
