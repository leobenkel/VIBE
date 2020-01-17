package com.leobenkel.vibe.core.Utils

import com.leobenkel.vibe.core.Services.Database
import zio.ZIO
import zio.console.Console

object SchemaTypes {
  type ID = String
  type Date = Long
  type WHERE_CLAUSE[A] = A => Boolean

  type TABLE_NAME = String
  type QueryZIO[A] = ZIO[Any with Database with Console, Throwable, A]

  def idFromString(s: String): ID = s //.toLong
}
