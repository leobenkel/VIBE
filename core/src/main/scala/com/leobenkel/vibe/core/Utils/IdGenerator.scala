package com.leobenkel.vibe.core.Utils

import java.time.ZoneOffset

import com.leobenkel.vibe.core.Utils.SchemaTypes._
import zio.ZIO
import zio.clock.Clock
import zio.random.Random

object IdGenerator {
  def getNowTime: ZIO[Any with Clock, Nothing, Long] = {
    ZIO
      .accessM[Clock](_.clock.currentDateTime)
      .map(_.withOffsetSameLocal(ZoneOffset.UTC)).map(_.toInstant.toEpochMilli)
  }

  def generateId(item: Object): ZIO[Any with Clock with Random, Nothing, (ID, Date)] = {
    for {
      seed <- ZIO.accessM[Random](_.random.nextInt)
      ts   <- getNowTime
    } yield {
      val concatId: ID = ts + "-" + Math.abs(item.hashCode) + "-" + Math.abs(seed)
      (concatId, ts)
    }
  }
}
