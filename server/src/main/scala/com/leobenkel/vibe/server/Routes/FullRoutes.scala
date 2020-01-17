package com.leobenkel.vibe.server.Routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.server.{Route, _}
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.server.Environment.{Config, LiveEnvironment}
import com.leobenkel.vibe.server.Messages.ToMessage
import com.leobenkel.vibe.server.Routes.Root.{HtmlRoute, ModelRootRoute}
import com.leobenkel.vibe.server.Routes.Utils._
import com.leobenkel.vibe.server.Schemas.ModelPickler
import com.leobenkel.vibe.server.Utils.ZIODirectives
import com.leobenkel.vibe.server.WebBoilerPlate.ActorSystem
import de.heikoseeberger.akkahttpupickle.UpickleSupport
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

trait FullRoutes
    extends RouteTraitWithChild with Directives with LiveEnvironment with UpickleSupport
    with ModelPickler with ZIODirectives
//    with ModelService
//    //TODO If you split your full route into different services, add them here
//    with HTMLService
    {

  def actor:  ActorSystem
  def config: Config.Service
//  private val runtime: DefaultRuntime = new DefaultRuntime() {}
  protected def env: Any with Database with Console with Clock with Random
  private val self: FullRoutes = this
//  private val log:              LoggingAdapter = Logging.getLogger(actor.actorSystem, this)
  private val staticContentDir: String = config.getLocalString("staticContentDir")

  override private[Routes] val getChildRoute: Seq[RouteTrait] = Seq(
    HtmlRoute(staticContentDir),
    new ModelRootRoute {
      override protected def env: Any with Database with Console with Clock with Random = self.env
    }
  )

  lazy override val url: String = ""

  lazy private val rejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder().handleNotFound {
        ToMessage.makeError(
          url = r => ToMessage.Url(r.unmatchedPath.toString),
          statusCodes = StatusCodes.NotFound,
          message = url => s"Path $url not found!"
        )
      }
      .result()

  lazy final override val route: Route = DebuggingDirectives.logRequest("Request") {
    handleRejections(rejectionHandler) {
      ignoreTrailingSlash(getChildRoute.map(_.route).reduce(_ ~ _))
    }
  }
}
