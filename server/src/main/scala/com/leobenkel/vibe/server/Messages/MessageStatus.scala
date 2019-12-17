package com.leobenkel.vibe.server.Messages

sealed trait MessageStatus

object MessageStatus {

  case object Success extends MessageStatus

  case class Failure(message: String) extends MessageStatus

}
