package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Schemas.Comment
import com.leobenkel.vibe.core.Utils.SchemaTypes._

trait Commentable extends ForeignAssociation[Commentable.FOREIGN_ID] {
  def id:         Commentable.PK
  def commentIds: Set[Comment.PK]
  lazy final val comments: QueryZIO[Seq[Comment]] = Comment.querySeveral(commentIds)
}

object Commentable {
  type PK = ID
  type FOREIGN_ID = ID
  type FOREIGN_TABLE = TABLE_NAME
}
