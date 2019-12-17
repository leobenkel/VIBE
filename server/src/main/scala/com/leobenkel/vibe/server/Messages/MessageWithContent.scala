package com.leobenkel.vibe.server.Messages

import io.circe._
import io.circe.syntax._

import scala.reflect.ClassTag

abstract class MessageWithContent[A: ClassTag](
  operation: String,
  status:    MessageStatus,
  fieldName: String
)(
  implicit encodeA: Encoder[A]
) extends Message(
      operation = operation,
      status = status
    ) {
  implicit private val encodeResult: Encoder[MessageWithContentForJson] =
    Encoder.forProduct4[MessageWithContentForJson, String, Boolean, Option[String], Json](
      "operation",
      "success",
      "errorMessage",
      fieldName
    ) {
      case MessageWithContentForJson(o, s, e, c) => (o, s, e, encodeA(c))
    }

  private case class MessageWithContentForJson(
    operation:    String,
    success:      Boolean,
    errorMessage: Option[String],
    content:      A
  )

  protected def extraContent: A

  private def toMessageWithContent(content: A): MessageWithContentForJson = {
    MessageWithContentForJson(
      operation = operation,
      success = getSuccessStatus,
      errorMessage = getErrorMessage,
      content = content
    )
  }

  override protected def asJsonObject: Json = {
    this.toMessageWithContent(extraContent).asJson
  }
}

object MessageWithContent {
  def apply[A: ClassTag](
    operation: String,
    status:    MessageStatus,
    fieldName: String
  )(
    content: => A
  )(
    implicit encodeA: Encoder[A]
  ): MessageWithContent[A] = {
    new MessageWithContent[A](operation, status, fieldName) {
      override protected def extraContent: A = content
    }
  }
}
