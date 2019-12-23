package com.leobenkel.vibe.core.Messages

import scala.reflect.ClassTag

trait MessageWithContent[A] extends Message {
  def classTagA: ClassTag[A]
//  implicit private val encodeResult: Encoder[MessageWithContentForJson] =
//    Encoder.forProduct4[MessageWithContentForJson, String, Boolean, Option[String], Json](
//      "operation",
//      "success",
//      "errorMessage",
//      fieldName
//    ) {
//      case MessageWithContentForJson(o, s, e, c) => (o, s, e, encodeA(c))
//    }

  def operation:  String
  def status:     MessageStatus
  def fieldName:  String
  def getContent: A

  protected case class MessageWithContentForJson(
    operation:    String,
    success:      Boolean,
    errorMessage: Option[String],
    content:      A
  )

  protected def toMessageWithContent(content: A): MessageWithContentForJson = {
    MessageWithContentForJson(
      operation = operation,
      success = getSuccessStatus,
      errorMessage = getErrorMessage,
      content = content
    )
  }

//  override protected def asJsonObject: Json = {
//    this.toMessageWithContent(extraContent).asJson
//  }
}

//object MessageWithContent {
//  def apply[A: ClassTag](
//    operation: String,
//    status:    MessageStatus,
//    fieldName: String
//  )(
//    content: => A
//  ): MessageWithContent[A] = {
//    new MessageWithContent[A](operation, status, fieldName) {
//      override protected def extraContent: A = content
//    }
//  }
//}
