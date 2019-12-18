package com.leobenkel.vibe.server.Routes.Utils

import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import akka.stream.Materializer
import com.leobenkel.vibe.core.Schemas.Traits.{SchemaBase, TableRef}
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME
import io.circe.Encoder
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

private[Routes] trait RouteSchema[PK, A <: SchemaBase[PK], INPUT] extends RouteTraitWithChild {
  def encoder:     Encoder[A]
  def tag:         ClassTag[A]
  def environment: Any with Database with Console with Clock with Random
  def getTableRef: TableRef[PK, A]
  def make(i: INPUT): RoutePutSchema.ZCREATE[A]
  def httpCreateSchemaForm(): Directive[INPUT]
  lazy private val tableName:  TABLE_NAME = getTableRef.getTableName
  lazy final override val url: String = tableName

  lazy final val unmarshaller: FromStringUnmarshaller[PK] = new FromStringUnmarshaller[PK] {
    override def apply(
      value: String
    )(
      implicit ec:  ExecutionContext,
      materializer: Materializer
    ): Future[PK] = {
      Future(getTableRef.idFromString(value))
    }
  }

  lazy private val self: RouteSchema[PK, A, INPUT] = this

  override private[Routes] def getChildRoute: Seq[RouteTrait] = {
    Seq(
      RouteGetSchema(self),
      RoutePutSchema(self)
    )
  }
}

object RouteSchema {
  case class FailedToParseException(msg: String) extends Exception(msg)
}
