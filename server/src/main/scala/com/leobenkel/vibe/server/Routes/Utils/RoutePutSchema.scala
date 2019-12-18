package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.leobenkel.vibe.core.Schemas.Traits.{SchemaBase, TableRef}
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME
import com.leobenkel.vibe.server.Messages.Message
import com.leobenkel.vibe.server.Routes.Utils.RoutePutSchema.ZCREATE
import com.leobenkel.vibe.server.Utils.MarshallerWrap
import io.circe.Encoder
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.{Task, ZIO}

import scala.reflect.ClassTag

trait RoutePutSchema[PK, A <: SchemaBase[PK], INPUT] extends RouteTrait {
  lazy final override val url:    String = "add"
  lazy final override val method: HttpMethod = HttpMethods.PUT
  protected def getTableRef: TableRef[PK, A]
  protected def environment: Any with Database with Console with Clock with Random
  protected def encoder:     Encoder[A]
  protected def tag:         ClassTag[A]
  lazy private val tableName: TABLE_NAME = getTableRef.getTableName

  protected type Z_CREATE = ZCREATE[A]

  final override def route: Route = {
    path(url) {
      (put | post) {
        httpCreateSchemaForm().tapply { a =>
          complete {
            implicit val c: ClassTag[A] = tag
            implicit val e: Encoder[A] = encoder
            implicit val m: Marshaller[Task[(Boolean, A)], HttpResponse] =
              MarshallerWrap.boolean[A](
                operation = getFullUrl,
                tableName = tableName,
                fieldName = "created",
                missingErrorMessage = s"Cannot create '$tableName'"
              )

            (for {
              newItem <- make(a)
              b       <- getTableRef.insert(newItem)
            } yield {
              (b, newItem)
            }).provide(environment)
          }
        }
      }
    } ~ super.route
  }

  protected def httpCreateSchemaForm(): Directive[INPUT]

  protected def make(i: INPUT): Z_CREATE

  final override protected def methodGetOutput(): Message =
    error("PUT/POST method only")
}

object RoutePutSchema {
  type ZCREATE[A] = ZIO[Any with Clock with Random, Throwable, A]

  def apply[PK, A <: SchemaBase[PK], INPUT](
    self: RouteSchema[PK, A, INPUT]
  ): RoutePutSchema[PK, A, INPUT] =
    new RoutePutSchema[PK, A, INPUT] {
      lazy final override val getTableRef: TableRef[PK, A] = self.getTableRef
      lazy final override val environment: Any with Database with Console with Clock with Random =
        self.environment
      lazy final override val parent:            Option[RouteTraitWithChild] = Some(self)
      lazy final override protected val encoder: Encoder[A] = self.encoder
      lazy final override protected val tag:     ClassTag[A] = self.tag

      override protected def httpCreateSchemaForm(): Directive[INPUT] = self.httpCreateSchemaForm()
      override protected def make(i: INPUT): Z_CREATE = self.make(i)
    }
}
