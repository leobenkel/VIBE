package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Utils.IdGenerator
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import zio.ZIO
import zio.clock.Clock

trait SchemaBase[PRIMARY_KEY] {
  type PK = PRIMARY_KEY
  def id:                PRIMARY_KEY
  def creationTimestamp: Date
  //noinspection MutatorLikeMethodIsParameterless
  def updateTimestamp: Date
}

object SchemaBase {}
