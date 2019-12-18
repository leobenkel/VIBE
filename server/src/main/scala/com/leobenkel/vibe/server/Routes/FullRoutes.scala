package com.leobenkel.vibe.server.Routes

import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.server.{Route, _}
import com.leobenkel.vibe.core.Schemas.Tag
import com.leobenkel.vibe.core.Schemas.Traits.TableRef
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.server.Environment.LiveEnvironment
import com.leobenkel.vibe.server.Routes.Root.RootRoute
import com.leobenkel.vibe.server.Routes.Utils.RoutePutSchema.ZCREATE
import com.leobenkel.vibe.server.Routes.Utils._
import com.leobenkel.vibe.server.Schemas.ModelPickler
import com.leobenkel.vibe.server.Utils.ZIODirectives
import de.heikoseeberger.akkahttpupickle.UpickleSupport
import io.circe.Encoder
import io.circe.generic.auto._
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

import scala.reflect.ClassTag

trait FullRoutes
    extends RouteTraitWithChild with Directives with LiveEnvironment with UpickleSupport
    with ModelPickler with ZIODirectives
//    with ModelService
//    //TODO If you split your full route into different services, add them here
//    with HTMLService
    {
//  private val runtime: DefaultRuntime = new DefaultRuntime() {}
  protected def env: Any with Database with Console with Clock with Random

  override private[Routes] val getChildRoute: Seq[RouteTrait] = Seq(
    RootRoute(this),
    new RouteSchema[Tag.PK, Tag, (String, Boolean)] {
      lazy final override val encoder: Encoder[Tag] = implicitly
      lazy final override val tag:     ClassTag[Tag] = implicitly
      lazy final override val environment: Any with Database with Console with Clock with Random =
        env
      lazy final override val getTableRef: TableRef[Tag.PK, Tag] = Tag

      override def make(i: (String, Boolean)): ZCREATE[Tag] =
        Tag.apply(i._1, i._2)

      override def httpCreateSchemaForm(): Directive[(String, Boolean)] =
        formFields('name.as[String], 'isVisible.as[Boolean])
    }
  )

  lazy override val url: String = ""

  override val route: Route = DebuggingDirectives.logRequest("Request") {
    ignoreTrailingSlash {
      getChildRoute.map(_.route).reduce(_ ~ _) /*~
        path("helloWorld") {
          complete {
            case class Output(str: String)
            val c = MarshallerWrap[Output]("plop", "hahaha")
            implicit val m: Marshaller[Task[Output], HttpResponse] = c.zioMarshaller
            for {
              count <- UIO(2)
            } yield Output(s"Yay! Count: $count at ${LocalDateTime.now}")
          }
        }*/
    }
  }
}
