package com.leobenkel.vibe.core.Messages

case class ContentS[A](
  items:  Seq[A],
  length: Int
)
