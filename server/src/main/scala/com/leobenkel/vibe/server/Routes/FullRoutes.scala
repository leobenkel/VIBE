package com.leobenkel.vibe.server.Routes

import java.time.LocalDateTime

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.server.{Route, _}
import com.leobenkel.vibe.server.Environment.LiveEnvironment
import com.leobenkel.vibe.server.Messages.Message
import com.leobenkel.vibe.server.Routes.Root.RootRoute
import com.leobenkel.vibe.server.Routes.Utils.{RouteTrait, RouteTraitWithChild}
import com.leobenkel.vibe.server.Schemas.ModelPickler
import com.leobenkel.vibe.server.Utils.ZIODirectives
import de.heikoseeberger.akkahttpupickle.UpickleSupport
import io.circe.generic.auto._
import zio.{Task, UIO}

trait FullRoutes
    extends RouteTraitWithChild with Directives with LiveEnvironment with UpickleSupport
    with ModelPickler with ZIODirectives
//    with ModelService
//    //TODO If you split your full route into different services, add them here
//    with HTMLService
    {
//  private val runtime: DefaultRuntime = new DefaultRuntime() {}

  override private[Routes] val getChildRoute: Seq[RouteTrait] = Seq(
    RootRoute(this),
    new RouteTrait() {
      override val url: String = "helloWorld2"

      override protected def methodGetOutput(): Message = ???
    }
  )

  lazy override val url: String = ""

  override val route: Route = DebuggingDirectives.logRequest("Request") {
    ignoreTrailingSlash {
      getChildRoute.map(_.route).reduce(_ ~ _) ~
        path("helloWorld") {
          complete {
            case class Output(str: String)
            val c = MarshallerWrap[Output]("plop", "hahaha")
            implicit val m: Marshaller[Task[Output], HttpResponse] = c.zioMarshaller
            for {
              count <- UIO(2)
            } yield Output(s"Yay! Count: $count at ${LocalDateTime.now}")
          }
        }
    }
  }
}
