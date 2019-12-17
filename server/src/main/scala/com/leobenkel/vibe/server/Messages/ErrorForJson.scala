package com.leobenkel.vibe.server.Messages

import io.circe.Error

import scala.util.Try

case class ErrorForJson(
  className:    Option[String],
  message:      Option[String],
  messageCause: Option[String]
)

object ErrorForJson {
  def apply(e: Error): ErrorForJson = {
    ErrorForJson(
      message = Try(e.getMessage).toOption.flatMap(Option(_)),
      messageCause = Try(e.getCause.getMessage).toOption.flatMap(Option(_)),
      className = Try(e.getClass.getCanonicalName).toOption.flatMap(Option(_))
    )
  }
}
