package com.leobenkel.vibe.core.TestUtils

import com.leobenkel.vibe.core.Services.Database
import zio.Runtime
import zio.clock.Clock
import zio.internal.{Platform, PlatformLive}
import zio.random.Random

case class TestRuntime() extends Runtime[TestRuntime.ENV] {
  override val environment: TestRuntime.ENV = TestEnvironment()
  override val platform:    Platform = PlatformLive.Default
}

object TestRuntime {
  type ENV = Any with Clock with Database with Random
}

case class TestEnvironment() extends Clock.Live with Random.Live with Database {
  override def database: Database.Service = ???
}
