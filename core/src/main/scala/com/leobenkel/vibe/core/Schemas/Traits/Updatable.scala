package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Utils.IdGenerator
import com.leobenkel.vibe.core.Utils.SchemaTypes.{Date, QueryZIO}
import zio.ZIO
import zio.clock.Clock
import zio.console.Console

trait Updatable[PRIMARY_KEY, SELF <: SchemaBase[PRIMARY_KEY] with Updatable[PRIMARY_KEY, SELF]] {
  private type PK = PRIMARY_KEY
  def get: SELF
  def update(updateTimestamp: Date): SELF
  def getTableTool: TableRef[PK, SELF]
  final def save(): QueryZIO[Boolean] = getTableTool.insert(this.get)
  final def refreshTimestamp: ZIO[Any with Clock, Nothing, SELF] =
    IdGenerator.getNowTime.map(ts => this.update(updateTimestamp = ts))
}
