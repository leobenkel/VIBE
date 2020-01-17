package com.leobenkel.vibe.core.Messages

trait Message {
  def operation: String
  def status:    MessageStatus

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

  protected def toSimpleMessage: SimpleMessage = {
    SimpleMessage(
      operation = operation,
      success = getSuccessStatus,
      errorMessage = getErrorMessage
    )
  }
}
