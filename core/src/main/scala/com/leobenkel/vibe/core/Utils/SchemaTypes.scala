package com.leobenkel.vibe.core.Utils

import com.leobenkel.vibe.core.Services.Database
import zio.ZIO

object SchemaTypes {
  type ID = Long
  type Date = Long
  type WHERE_CLAUSE[A] = A => Boolean

  type TABLE_NAME = String
  type QueryZIO[A] = ZIO[Any with Database, Throwable, A]
}
