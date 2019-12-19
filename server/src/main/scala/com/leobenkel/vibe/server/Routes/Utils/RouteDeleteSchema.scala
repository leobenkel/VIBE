package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.common.NameReceptacle
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import com.leobenkel.vibe.core.Schemas.Traits.{SchemaBase, TableRef}
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME
import com.leobenkel.vibe.server.Messages.Message
import com.leobenkel.vibe.server.Utils.MarshallerWrap
import io.circe.Encoder
import zio.Task
import zio.console.Console

import scala.reflect.ClassTag

trait RouteDeleteSchema[PK, A <: SchemaBase[PK]] extends RouteTrait {
  lazy final override val url:    String = "delete"
  lazy final override val method: HttpMethod = HttpMethods.DELETE
  protected def getTableRef:  TableRef[PK, A]
  protected def encoder:      Encoder[A]
  protected def tag:          ClassTag[A]
  protected def environment:  Any with Database with Console
  protected def unmarshaller: FromStringUnmarshaller[PK]

  lazy private val tableName: TABLE_NAME = getTableRef.getTableName

  final override def route: Route = {
    path(url) {
      delete {
        methodGetRetrieveParameters().tapply { id =>
          complete {
            implicit val c: ClassTag[A] = tag
            implicit val e: Encoder[A] = encoder
            implicit val m: Marshaller[Task[(Option[A], Boolean)], HttpResponse] =
              MarshallerWrap.booleanOpt[A](
                operation = getFullUrl,
                tableName = tableName,
                fieldName = "deleted",
                notFoundErrorMessage = s"Cannot find a '$tableName' for ID:'${id._1}'",
                notDeletedErrorMessage = s"Failed to delete from '$tableName' for ID:'${id._1}'"
              )
            methodDelete(id)
          }
        }
      }
    } ~ super.route
  }

  protected def methodGetParameters(): Seq[NameReceptacle[_]] = Seq('id)

  protected def methodGetRetrieveParameters(): Directive1[PK] =
    parameter('id.as[PK](unmarshaller))

  protected def methodDelete(id: Tuple1[PK]): Task[(Option[A], Boolean)] = {
    (for {
      itemToDelete <- getTableRef.queryOne(id._1)
      deletion     <- getTableRef.deleteRow(id._1)
    } yield {
      (itemToDelete, deletion)
    }).provide(environment)
  }

  final override protected def methodGetOutput(): Message =
    error("DELETE method only")
}

object RouteDeleteSchema {
  def apply[PK, A <: SchemaBase[PK]](self: RouteSchema[PK, A, _]): RouteDeleteSchema[PK, A] =
    new RouteDeleteSchema[PK, A] {
      lazy final override val getTableRef:  TableRef[PK, A] = self.getTableRef
      lazy final override val encoder:      Encoder[A] = self.encoder
      lazy final override val tag:          ClassTag[A] = self.tag
      lazy final override val environment:  Any with Database with Console = self.environment
      lazy final override val unmarshaller: FromStringUnmarshaller[PK] = self.unmarshaller
      lazy final override val parent:       Option[RouteTraitWithChild] = Some(self)
    }
}
