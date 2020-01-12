package com.leobenkel.vibe.core.Utils

import zio.ZIO
import zio.console.Console

object Log {
  def apply(msg: String): ZIO[Console, Nothing, Unit] =
    ZIO.accessM[zio.console.Console](_.console.putStrLn(msg))
}
