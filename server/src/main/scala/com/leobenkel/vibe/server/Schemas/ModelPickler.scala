package com.leobenkel.vibe.server.Schemas

import com.leobenkel.vibe.core.Schemas._
import upickle.default.{macroRW, ReadWriter => RW}

trait ModelPickler {
  implicit val CommentRW:   RW[Comment] = macroRW
  implicit val IdeaRW:      RW[Idea] = macroRW
  implicit val SkillRW:     RW[Skill] = macroRW
  implicit val TagRW:       RW[Tag] = macroRW
  implicit val UserRW:      RW[User] = macroRW
  implicit val UserVotesRW: RW[UserVotes] = macroRW
}
