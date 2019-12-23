package com.leobenkel.vibe.core.Messages

trait Message {
  protected case class SimpleMessage(
    operation:    String,
    success:      Boolean,
    errorMessage: Option[String]
  )

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

//  protected def asJsonObject: Json = {
//    this.toSimpleMessage.asJson
//  }

//  final def toJsonString: String = {
//    this.asJsonObject.toString
//  }

//  override type T = String

//  override def value: T = this.toJsonString

//  implicit override def marshaller: ToResponseMarshaller[T] =
//    Marshaller.fromToEntityMarshaller(status = StatusCodes.OK, headers = Nil)
}

//object Message {
//  def success(operation: String): Message = {
//    new Message(operation, MessageStatus.Success) {}
//  }
//
//  def failed(
//    operation:    String,
//    errorMessage: String
//  ): Message = {
//    new Message(operation, MessageStatus.Failure(errorMessage)) {}
//  }
//}
