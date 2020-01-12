package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.common.NameReceptacle
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import com.leobenkel.vibe.core.Messages.Message
import com.leobenkel.vibe.core.Schemas.Traits.{SchemaT, TableRef}
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME
import com.leobenkel.vibe.server.Messages.MessageSerializer
import com.leobenkel.vibe.server.Utils.MarshallerWrap
import io.circe.Encoder
import zio.Task
import zio.console.Console

import scala.reflect.ClassTag

trait RouteGetSchema[PK, ROW <: SchemaT[PK, ROW]] extends RouteTrait {
  lazy final override val url:    String = "get"
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
        methodGetRetrieveParameters().tapply { methodGetParameters =>
          complete {
            implicit val c: ClassTag[ROW] = tag
            implicit val e: Encoder[ROW] = encoder
            implicit val m: Marshaller[Task[Option[ROW]], HttpResponse] = MarshallerWrap.apply[ROW](
              operation = getFullUrl,
              tableName = tableName,
              missingErrorMessage = s"Cannot find a '$tableName' for ID:'${methodGetParameters._1}'"
            )
            methodGetOutput(methodGetParameters)
          }
        }
      }
    } ~ super.route
  }

  protected def methodGetParameters(): Seq[NameReceptacle[_]] = Seq('id)

  protected def methodGetRetrieveParameters(): Directive1[PK] =
    parameter('id.as[PK](unmarshaller))

  protected def methodGetOutput(methodGetParameters: Tuple1[PK]): Task[Option[ROW]] = {
    getTableRef
      .queryOne(methodGetParameters._1)
      .provide(environment)
  }

  protected def methodGetOutput(): MessageSerializer = {
    errorWithContent[Seq[String]](
      errorMessage = "Missing GET parameters",
      contentFieldName = "missingGetParameters"
    ) {
      methodGetParameters().map(_.name)
    }
  }
}

object RouteGetSchema {
  def apply[PK, ROW <: SchemaT[PK, ROW], INPUT](
    self: RouteSchema[PK, ROW, INPUT]
  ): RouteGetSchema[PK, ROW] =
    new RouteGetSchema[PK, ROW] {
      lazy final override val getTableRef:  TableRef[PK, ROW] = self.getTableRef
      lazy final override val encoder:      Encoder[ROW] = self.encoder
      lazy final override val tag:          ClassTag[ROW] = self.tag
      lazy final override val environment:  Any with Database with Console = self.environment
      lazy final override val unmarshaller: FromStringUnmarshaller[PK] = self.unmarshaller
      lazy final override val parent:       Option[RouteTraitWithChild] = Some(self)
    }
}
