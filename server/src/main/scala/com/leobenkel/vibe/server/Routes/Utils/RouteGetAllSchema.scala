package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.common.NameReceptacle
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME
import com.leobenkel.vibe.server.Messages.MessageSerializer
import com.leobenkel.vibe.server.Utils.MarshallerWrap
import io.circe.Encoder
import zio.Task
import zio.console.Console

import scala.reflect.ClassTag

trait RouteGetAllSchema[PK, ROW <: SchemaT[PK, ROW]] extends RouteTrait {
  lazy final override val url:    String = "all"
  lazy final override val method: HttpMethod = HttpMethods.GET
  protected def getTableRef:  TableRef[PK, ROW]
  protected def encoder:      Encoder[ROW]
  protected def tag:          ClassTag[ROW]
  protected def environment:  Any with Database with Console
  protected def unmarshaller: FromStringUnmarshaller[PK]

  lazy private val tableName: TABLE_NAME = getTableRef.getTableName

  final override def route: Route = {
    path(url) {
      get {
        complete {
          implicit val c: ClassTag[ROW] = tag
          implicit val e: Encoder[ROW] = encoder
          implicit val m: Marshaller[Task[Seq[ROW]], HttpResponse] = MarshallerWrap.collection[ROW](
            operation = getFullUrl,
            tableName = tableName,
            missingErrorMessage = s"Cannot get all rows from '$tableName'"
          )
          methodGetAll()
        }
      }
    }
  }

  protected def methodGetParameters(): Seq[NameReceptacle[_]] = Seq('id)

  protected def methodGetRetrieveParameters(): Directive1[PK] =
    parameter('id.as[PK](unmarshaller))

  protected def methodGetAll(): Task[Seq[ROW]] = {
    getTableRef
      .queryAll()
      .provide(environment)
  }

  protected def methodGetOutput(): MessageSerializer = ???
}

object RouteGetAllSchema {
  def apply[PK, ROW <: SchemaT[PK, ROW], INPUT](
    self: RouteSchema[PK, ROW, INPUT]
  ): RouteGetAllSchema[PK, ROW] =
    new RouteGetAllSchema[PK, ROW] {
      lazy final override val getTableRef:  TableRef[PK, ROW] = self.getTableRef
      lazy final override val encoder:      Encoder[ROW] = self.encoder
      lazy final override val tag:          ClassTag[ROW] = self.tag
      lazy final override val environment:  Any with Database with Console = self.environment
      lazy final override val unmarshaller: FromStringUnmarshaller[PK] = self.unmarshaller
      lazy final override val parent:       Option[RouteTraitWithChild] = Some(self)
    }
}
