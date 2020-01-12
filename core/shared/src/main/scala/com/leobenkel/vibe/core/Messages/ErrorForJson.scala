package com.leobenkel.vibe.core.Messages

import scala.util.Try

case class ErrorForJson(
  className:    Option[String],
  message:      Option[String],
  messageCause: Option[String]
)

object ErrorForJson {
  def apply(e: Throwable): ErrorForJson = {
    ErrorForJson(
      message = Try(e.getMessage).toOption.flatMap(Option(_)),
      messageCause = Try(e.getCause.getMessage).toOption.flatMap(Option(_)),
      className = Try(e.getClass.getCanonicalName).toOption.flatMap(Option(_))
    )
  }
}
