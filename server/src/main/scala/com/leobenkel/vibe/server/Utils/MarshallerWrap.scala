package com.leobenkel.vibe.server.Utils

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.HttpResponse
import com.leobenkel.vibe.server.Messages._
import io.circe.{Encoder, Json}
import zio._

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

object MarshallerWrap extends DefaultRuntime {
  def apply[A: ClassTag](
    operation:           String,
    tableName:           String,
    missingErrorMessage: => String
  )(
    implicit encoder: Encoder[A]
  ): Marshaller[Task[Option[A]], HttpResponse] = {
    def zioMarshaller(
      implicit m1: Marshaller[MessageWithContent[A], HttpResponse],
      m2:          Marshaller[Message, HttpResponse]
    ): Marshaller[Task[Option[A]], HttpResponse] =
      Marshaller { _: ExecutionContext => a: Task[Option[A]] =>
        {
          val r: ZIO[Any, Throwable, List[Marshalling[HttpResponse]]] = a.foldM(
            (e: Throwable) => {
              Task.fromFuture(implicit ec => m2(ErrorMessage(operation)(e.toString)))
            }, {
              case Some(a) =>
                val wrappedM = MessageWithContent(
                  operation = operation,
                  status = MessageStatus.Success,
                  fieldName = tableName
                )(a)
                Task.fromFuture(implicit ec => m1(wrappedM))
              case None =>
                Task.fromFuture(implicit ec => m2(ErrorMessage(operation)(missingErrorMessage)))
            }
          )

          val p = scala.concurrent.Promise[List[Marshalling[HttpResponse]]]()

          unsafeRunAsync(r)(_.fold(e => p.failure(e.squash), output => p.success(output)))

          p.future
        }
      }
    zioMarshaller
  }

  def collection[A: ClassTag](
    operation:           String,
    tableName:           String,
    missingErrorMessage: => String
  )(
    implicit encoder: Encoder[A]
  ): Marshaller[Task[Seq[A]], HttpResponse] = {
    case class Results(results: Seq[A])
    def zioMarshaller(
      implicit m1: Marshaller[MessageWithContent[Results], HttpResponse],
      m2:          Marshaller[Message, HttpResponse]
    ): Marshaller[Task[Seq[A]], HttpResponse] =
      Marshaller { _: ExecutionContext => a: Task[Seq[A]] =>
        {
          val r: ZIO[Any, Throwable, List[Marshalling[HttpResponse]]] = a.foldM(
            (e: Throwable) => {
              Task.fromFuture(implicit ec => m2(ErrorMessage(operation)(e.toString)))
            }, { r =>
              implicit val encoderR: Encoder[Results] =
                Encoder.forProduct2[Results, Seq[Json], Int]("items", "length") {
                  case Results(r) => (r.map(rr => encoder.apply(rr)), r.length)
                }

              val wrappedM = MessageWithContent(
                operation = operation,
                status = MessageStatus.Success,
                fieldName = tableName
              )(Results(r))
              Task.fromFuture(implicit ec => m1(wrappedM))
            }
          )

          val p = scala.concurrent.Promise[List[Marshalling[HttpResponse]]]()

          unsafeRunAsync(r)(_.fold(e => p.failure(e.squash), output => p.success(output)))

          p.future
        }
      }
    zioMarshaller
  }

  def boolean[A: ClassTag](
    operation:           String,
    tableName:           String,
    fieldName:           String,
    missingErrorMessage: => String
  )(
    implicit encoder: Encoder[A]
  ): Marshaller[Task[(A, Boolean)], HttpResponse] = {
    def zioMarshaller(
      implicit m1: Marshaller[MessageWithContent[A], HttpResponse],
      m2:          Marshaller[Message, HttpResponse]
    ): Marshaller[Task[(A, Boolean)], HttpResponse] =
      Marshaller { _: ExecutionContext => a: Task[(A, Boolean)] =>
        {
          val r: ZIO[Any, Throwable, List[Marshalling[HttpResponse]]] = a.foldM(
            (e: Throwable) => {
              Task.fromFuture(implicit ec => m2(ErrorMessage(operation)(e.toString)))
            }, {
              case (item, true) =>
                val wrappedM = MessageWithContent(
                  operation = operation,
                  status = MessageStatus.Success,
                  fieldName = s"$tableName-$fieldName"
                )(item)
                Task.fromFuture(implicit ec => m1(wrappedM))
              case (_, false) =>
                Task.fromFuture(implicit ec => m2(ErrorMessage(operation)(missingErrorMessage)))
            }
          )

          val p = scala.concurrent.Promise[List[Marshalling[HttpResponse]]]()

          unsafeRunAsync(r)(_.fold(e => p.failure(e.squash), output => p.success(output)))

          p.future
        }
      }
    zioMarshaller
  }

  def booleanOpt[A: ClassTag](
    operation:              String,
    tableName:              String,
    fieldName:              String,
    notFoundErrorMessage:   => String,
    notDeletedErrorMessage: => String
  )(
    implicit encoder: Encoder[A]
  ): Marshaller[Task[(Option[A], Boolean)], HttpResponse] = {
    def zioMarshaller(
      implicit m1: Marshaller[MessageWithContent[A], HttpResponse],
      m2:          Marshaller[Message, HttpResponse]
    ): Marshaller[Task[(Option[A], Boolean)], HttpResponse] =
      Marshaller { _: ExecutionContext => a: Task[(Option[A], Boolean)] =>
        {
          val r: ZIO[Any, Throwable, List[Marshalling[HttpResponse]]] = a.foldM(
            (e: Throwable) => {
              Task.fromFuture(implicit ec => m2(ErrorMessage(operation)(e.toString)))
            }, {
              case (Some(item), true) =>
                val wrappedM = MessageWithContent(
                  operation = operation,
                  status = MessageStatus.Success,
                  fieldName = s"$tableName-$fieldName"
                )(item)
                Task.fromFuture(implicit ec => m1(wrappedM))
              case (None, _) =>
                Task.fromFuture(implicit ec => m2(ErrorMessage(operation)(notFoundErrorMessage)))
              case (None, false) =>
                Task.fromFuture(implicit ec => m2(ErrorMessage(operation)(notDeletedErrorMessage)))
            }
          )

          val p = scala.concurrent.Promise[List[Marshalling[HttpResponse]]]()

          unsafeRunAsync(r)(_.fold(e => p.failure(e.squash), output => p.success(output)))

          p.future
        }
      }
    zioMarshaller
  }
}
