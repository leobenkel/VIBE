package com.leobenkel.vibe.server.Messages

import io.circe.Encoder

import scala.reflect.ClassTag

object ErrorMessage {
  def apply(operation: => String)(message: => String): Message = {
    Message.failed(operation, message)
  }

  def withContent[A: ClassTag](
    operation: String
  )(
    message:   String,
    fieldName: String
  )(
    content: => A
  )(
    implicit encoder: Encoder[A]
  ): MessageWithContent[A] = {
    MessageWithContent[A](
      operation,
      MessageStatus.Failure(message),
      fieldName
    )(
      content
    )
  }
}
