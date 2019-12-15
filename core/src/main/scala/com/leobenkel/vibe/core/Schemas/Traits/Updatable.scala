package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Utils.IdGenerator
import com.leobenkel.vibe.core.Utils.SchemaTypes.Date
import zio.ZIO
import zio.clock.Clock

trait Updatable[SELF] {

  def update(updateTimestamp: Date): SELF

  final def refreshTimestamp: ZIO[Any with Clock, Nothing, SELF] =
    IdGenerator.getNowTime.map(ts => this.update(updateTimestamp = ts))
}
