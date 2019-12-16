package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.IdGenerator
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import zio.ZIO
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

case class Comment(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  authorId:          User.PK,
  content:           String,
  attachedToId:      Commentable.FOREIGN_ID,
  attachedToTable:   Commentable.FOREIGN_TABLE,
  commentIds:        Set[Comment.PK]
) extends SchemaBase[ID] with Commentable with Updatable[ID, Comment] {
  override def getTableName: TABLE_NAME = Comment.getTableName

  lazy final val getParent: ZIO[Any with Database with Console, Throwable, Option[Commentable]] = {
    TableRef[Commentable.FOREIGN_ID, Commentable](attachedToTable)
      .flatMap(_.queryOne(attachedToId))
  }

  override def update(updateTimestamp: Date): Comment = {
    this.update(updateTimestamp = updateTimestamp)
  }

  lazy final override val get:          Comment = this
  lazy final override val getTableTool: TableRef[PK, Comment] = Comment
}

object Comment extends TableRef[ID, Comment] {
  override def getTableName: TABLE_NAME = "comments"

  def apply(
    author:     User,
    content:    String,
    attachedTo: Commentable
  ): ZIO[Any with Clock with Random, Nothing, Comment] =
    IdGenerator.generateId((author.id, content, attachedTo.id, attachedTo.getTableName)).map {
      case (id, date) =>
        Comment(
          id = id,
          creationTimestamp = date,
          updateTimestamp = date,
          authorId = author.id,
          content = content,
          attachedToId = attachedTo.id,
          attachedToTable = attachedTo.getTableName,
          commentIds = Set.empty
        )
    }
}
