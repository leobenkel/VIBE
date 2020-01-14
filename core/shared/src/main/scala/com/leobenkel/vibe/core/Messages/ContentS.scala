package com.leobenkel.vibe.core.Messages

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

case class ContentS[A](
  items:  Seq[A],
  length: Int
)
