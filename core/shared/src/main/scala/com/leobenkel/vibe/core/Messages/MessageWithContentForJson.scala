package com.leobenkel.vibe.core.Messages

case class MessageWithContentForJson[A](
  operation:    String,
  success:      Boolean,
  errorMessage: Option[String],
  content:      A
)
