package com.leobenkel.vibe.core.Messages

case class SimpleMessage(
  operation:    String,
  success:      Boolean,
  errorMessage: Option[String]
)
