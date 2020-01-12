package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.Log
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME
import com.leobenkel.vibe.server.Messages.{MessageSerializer, ToMessage}
import com.leobenkel.vibe.server.Routes.Utils.RoutePutSchema.ZCREATE
import com.leobenkel.vibe.server.Utils.MarshallerWrap
import io.circe.Encoder
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

import scala.reflect.ClassTag

trait RoutePutSchema[PK, ROW <: SchemaT[PK, ROW], INPUT] extends RouteTrait {
  lazy final override val url:    String = "add"
  lazy final override val method: HttpMethod = HttpMethods.PUT
  protected def getTableRef: TableRef[PK, ROW]
  protected def environment: Any with Database with Console with Clock with Random
  protected def encoder:     Encoder[ROW]
  protected def tag:         ClassTag[ROW]
  lazy private val tableName: TABLE_NAME = getTableRef.getTableName

  protected type Z_CREATE = ZCREATE[PK, ROW]

  private val rejectionHandler: RejectionHandler = RejectionHandler
    .newBuilder()
    .handle {
      case missing: MissingFormFieldRejection =>
        ToMessage.makeError(
          statusCodes = StatusCodes.NotAcceptable,
          message = s"Form is missing field: '${missing.fieldName}'"
        )
      case malformed: MalformedFormFieldRejection =>
        ToMessage.makeError(
          statusCodes = StatusCodes.NotAcceptable,
          message = s"Form is malformed for field: '${malformed.fieldName}': " +
            s"${malformed.errorMsg}"
        )
    }
    .handleAll { errors: Seq[Rejection] =>
      ToMessage.makeError(
        statusCodes = StatusCodes.NotAcceptable,
        message = errors.map(_.toString).mkString(", ")
      )
    }
    .result()

  final override def route: Route = {
    path(url) {
      (put | post) {
        handleRejections(rejectionHandler) {
          httpCreateSchemaForm
            .tapply { a =>
              complete {
                implicit val c: ClassTag[ROW] = tag
                implicit val e: Encoder[ROW] = encoder
                implicit val m: Marshaller[Task[(ROW, Boolean)], HttpResponse] =
                  MarshallerWrap.boolean[ROW](
                    operation = getFullUrl,
                    tableName = tableName,
                    fieldName = "created",
                    missingErrorMessage = s"Cannot create '$tableName'"
                  )

                (for {
                  _ <- Log(
                    s"[HTTP][${method.name()}] hitting '$getFullUrl' " +
                      s"- Table: ${getTableRef.getTableName}"
                  )
                  newItem <- make(a)
                  b <- newItem
                    .save()
                    .fold(
                      ex => Log(s"Failed to insert record: ${ex.toString}").map(_ => false),
                      _ => UIO(true)
                    ).flatten
                } yield {
                  newItem -> b
                }).provide(environment)
              }
            }
        }
      }
    } ~ super.route
  }

  protected def httpCreateSchemaForm: Directive[INPUT]

  protected def make(i: INPUT): Z_CREATE

  final override protected def methodGetOutput(): MessageSerializer =
    error("PUT/POST method only")
}

object RoutePutSchema {
  type ZCREATE[PK, A <: SchemaT[PK, A]] = ZIO[Any with Clock with Random, Throwable, A]

  def apply[PK, ROW <: SchemaT[PK, ROW], INPUT](
    self: RouteSchema[PK, ROW, INPUT]
  ): RoutePutSchema[PK, ROW, INPUT] =
    new RoutePutSchema[PK, ROW, INPUT] {
      lazy final override val getTableRef: TableRef[PK, ROW] = self.getTableRef
      lazy final override val environment: Any with Database with Console with Clock with Random =
        self.environment
      lazy final override val parent:            Option[RouteTraitWithChild] = Some(self)
      lazy final override protected val encoder: Encoder[ROW] = self.encoder
      lazy final override protected val tag:     ClassTag[ROW] = self.tag

      lazy final override protected val httpCreateSchemaForm: Directive[INPUT] =
        self.httpCreateSchemaForm
      override protected def make(i: INPUT): Z_CREATE = self.make(i)
    }
}
