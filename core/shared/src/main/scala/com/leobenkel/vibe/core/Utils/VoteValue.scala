package com.leobenkel.vibe.core.Utils

import zio._

sealed trait VoteValue {
  def isUp:     Boolean
  def encoding: Int
}

object VoteValue {
  def parse(vote: Int): IO[RuntimeException, VoteValue] = vote match {
    case n if n == VoteDown.encoding => UIO(VoteDown)
    case n if n == VoteUp.encoding   => UIO(VoteUp)
    case v =>
      ZIO.fail(new RuntimeException(s"Cannot find value vote for value $v"))
  }

  case object VoteUp extends VoteValue {
    lazy final override val isUp:     Boolean = true
    lazy final override val encoding: Int = 1
  }

  case object VoteDown extends VoteValue {
    lazy final override val isUp:     Boolean = false
    lazy final override val encoding: Int = 0
  }

}
