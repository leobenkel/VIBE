package com.leobenkel.vibe.server.Messages

import com.leobenkel.vibe.core.Messages.MessageStatus
import io.circe.Encoder

import scala.reflect.ClassTag

object ToMessage {
  case class RichMessage[A: ClassTag](
    operation: String,
    status:    MessageStatus,
    fieldName: String
  )(
    content: => A
  )(
    implicit val encodeA: Encoder[A]
  ) extends RichMessageSerializer[A] {
    lazy final override val getContent: A = content
    lazy final override val classTagA:  ClassTag[A] = implicitly
  }

  //    operation: String
//  )(
//    message:   String,
//    fieldName: String
//  )(
//    content: => A
//  )(
//    implicit encoder: Encoder[A]
//  ): MessageWithContent[A] = {
//    MessageWithContent[A](
//      operation,
//      MessageStatus.Failure(message),
//      fieldName
//    )(
//      content
//    )
//  }
  case class ErrorMessage(operation: String)(message: => String) extends MessageSerializer {
    lazy final override val status: MessageStatus = MessageStatus.Failure(message)
  }

  case class RichErrorMessage[A: ClassTag](
    operation: String,
    message:   String,
    fieldName: String
  )(
    content: => A
  )(
    implicit val encodeA: Encoder[A]
  ) extends RichMessageSerializer[A] {
    lazy final override val getContent: A = content
    lazy final override val status:     MessageStatus = MessageStatus.Failure(message)
    lazy final override val classTagA:  ClassTag[A] = implicitly
  }

  //  case class ErrorMessage[A: ClassTag](
  //    operation: String
  //  )(
  //    message:   String,
  //    fieldName: String
  //  )(
  //    content: => A
  //  )
}
