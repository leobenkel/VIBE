package com.leobenkel.vibe.core.Messages

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

case class MessageWithContentForJson[A](
  operation:    String,
  success:      Boolean,
  errorMessage: Option[String],
  content:      A
)
