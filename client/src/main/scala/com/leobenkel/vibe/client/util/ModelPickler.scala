package com.leobenkel.vibe.client.util

import com.leobenkel.vibe.core.Messages._
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

//  implicit def contentSerializedRW[A](
//    implicit r: upickle.default.Reader[A],
//    w:          upickle.default.Writer[A]
//  ): RW[ContentSerialized[A]] = macroRW

//  implicit val CommentContent:   RW[ContentSerialized[Comment]] = macroRW
//  implicit val IdeaContent:      RW[ContentSerialized[Idea]] = macroRW
//  implicit val SkillContent:     RW[ContentSerialized[Skill]] = macroRW
//  implicit val TagContent:       RW[ContentSerialized[Tag]] = macroRW
//  implicit val UserContent:      RW[ContentSerialized[User]] = macroRW
//  implicit val UserVotesContent: RW[ContentSerialized[UserVotes]] = macroRW

  implicit val SimpleMessageRW: RW[SimpleMessage] = macroRW

//  implicit def messageWithContentForJsonRW[A](
//    implicit r: upickle.default.Reader[A],
//    w:          upickle.default.Writer[A]
//  ): RW[MessageWithContentForJson[A]] = macroRW

//  implicit val MessageWithContent: RW[MessageWithContent[_]] = macroRW
}
