package com.leobenkel.vibe.client.util

import com.leobenkel.vibe.core.Schemas._
import upickle.default.{macroRW, ReadWriter => RW}

/**
  * Here's where we define all of the model object's picklers and unpicklers.
  * You may want to move this to the shared project, though I like to keep them separately in case
  * you want to use a different method for marshalling json between the client and server
  */
object ModelPickler {
  implicit val CommentRW:   RW[Comment] = macroRW
  implicit val IdeaRW:      RW[Idea] = macroRW
  implicit val SkillRW:     RW[Skill] = macroRW
  implicit val TagRW:       RW[Tag] = macroRW
  implicit val UserRW:      RW[User] = macroRW
  implicit val UserVotesRW: RW[UserVotes] = macroRW
}
