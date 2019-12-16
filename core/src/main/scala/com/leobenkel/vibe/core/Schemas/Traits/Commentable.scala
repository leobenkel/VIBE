package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Schemas.Comment
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import zio.console.Console

trait Commentable extends ForeignAssociation[Commentable.FOREIGN_ID] {
  private type PRIMARY_KEY = Commentable.FOREIGN_ID
  def commentIds: Set[Comment.PK]
  lazy final val comments: QueryZIO[Seq[Comment]] = Comment.querySeveral(commentIds)
}

object Commentable {
  type FOREIGN_ID = ID
  type FOREIGN_TABLE = TABLE_NAME
}
