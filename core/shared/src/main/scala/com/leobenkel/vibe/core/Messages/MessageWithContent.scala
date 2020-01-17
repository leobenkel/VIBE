package com.leobenkel.vibe.core.Messages

import scala.reflect.ClassTag

trait MessageWithContent[A] extends Message {
  implicit def classTagA: ClassTag[A]
//  implicit def typeTagA:  TypeTag[A]

  def operation:  String
  def status:     MessageStatus
  def fieldName:  String
  def getContent: A

  protected def toMessageWithContent(content: A): MessageWithContentForJson[A] = {
    MessageWithContentForJson(
      operation = operation,
      success = getSuccessStatus,
      errorMessage = getErrorMessage,
      content = content
    )
  }
}
