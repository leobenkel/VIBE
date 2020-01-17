package com.leobenkel.vibe.server.Messages

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.StatusCodes
import com.leobenkel.vibe.core.Messages.{MessageWithContent, MessageWithContentForJson}
import io.circe.syntax._
import io.circe.{Encoder, Json}

trait RichMessageSerializer[A] extends MessageWithContent[A] with MessageSerializer {

  def encodeA: Encoder[A]

  implicit private val encodeResult: Encoder[MessageWithContentForJson[A]] =
    Encoder.forProduct4[MessageWithContentForJson[A], String, Boolean, Option[String], Json](
      "operation",
      "success",
      "errorMessage",
      fieldName
    ) {
      case MessageWithContentForJson(o, s, e, c) => (o, s, e, encodeA(c))
    }

  lazy final override val toJsonString: String =
    this.toMessageWithContent(getContent).asJson.noSpaces.toString

  final override type T = String

  lazy final override val value: T = this.toJsonString

  implicit lazy final override val marshaller: ToResponseMarshaller[T] =
    Marshaller.fromToEntityMarshaller(status = StatusCodes.OK, headers = Nil)
}
