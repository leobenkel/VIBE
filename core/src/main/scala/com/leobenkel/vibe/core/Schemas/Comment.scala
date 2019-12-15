package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits.{Commentable, SchemaBase, TableRef, Updatable}
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.IdGenerator
import zio.ZIO
import zio.clock.Clock
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
) extends SchemaBase[ID] with Commentable with Updatable[Comment] {
  override def getTableName: TABLE_NAME = Comment.getTableName

  lazy final val getParent: ZIO[Any with Database, Any, Option[Commentable]] = {
    ZIO
      .fromOption(
        TableRef(attachedToTable)
          .map(_.asInstanceOf[TableRef[Commentable.FOREIGN_ID, Commentable]])
          .map(_.queryOne(attachedToId))
      ).flatten
  }

  override def update(updateTimestamp: Date): Comment = {
    this.update(updateTimestamp = updateTimestamp)
  }
}

object Comment extends TableRef[ID, Comment] {
  override def getTableName: TABLE_NAME = "comments"

  override def queryOne(id: ID): QueryZIO[Option[Comment]] = ???

  override def querySeveral(id: Set[ID]): QueryZIO[Seq[Comment]] = ???

  override def querySpecific(whereClause: WHERE_CLAUSE[Comment]): QueryZIO[Seq[Comment]] = ???

  override def deleteRow(id: ID): QueryZIO[Boolean] = ???

  override def insert(row: Comment): QueryZIO[Boolean] = ???

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
