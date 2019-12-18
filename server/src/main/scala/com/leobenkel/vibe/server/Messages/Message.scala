package com.leobenkel.vibe.server.Messages

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

abstract class Message(
  operation: String,
  status:    MessageStatus
) {

  private case class SimpleMessage(
    operation:    String,
    success:      Boolean,
    errorMessage: Option[String]
  )

  final protected def getErrorMessage: Option[String] = {
    this.status match {
      case MessageStatus.Success          => None
      case MessageStatus.Failure(message) => Some(message)
    }
  }

  final protected def getSuccessStatus: Boolean = {
    this.status match {
      case MessageStatus.Success    => true
      case MessageStatus.Failure(_) => false
    }
  }

  private def toSimpleMessage: SimpleMessage = {
    SimpleMessage(
      operation = operation,
      success = getSuccessStatus,
      errorMessage = getErrorMessage
    )
  }

  protected def asJsonObject: Json = {
    this.toSimpleMessage.asJson
  }

  final def toJsonString: String = {
    this.asJsonObject.toString
  }
}

object Message {
  def success(operation: String): Message = {
    new Message(operation, MessageStatus.Success) {}
  }

  def failed(
    operation:    String,
    errorMessage: String
  ): Message = {
    new Message(operation, MessageStatus.Failure(errorMessage)) {}
  }
}
