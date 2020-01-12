package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Utils.SchemaTypes._

trait SchemaBase[PRIMARY_KEY] {
  type PK = PRIMARY_KEY
  def id:                PRIMARY_KEY
  def creationTimestamp: Date
  //noinspection MutatorLikeMethodIsParameterless
  def updateTimestamp: Date
}

object SchemaBase {}
