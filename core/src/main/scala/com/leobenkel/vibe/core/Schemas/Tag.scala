package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase._
import com.leobenkel.vibe.core.Schemas.Traits.TableRef.TABLE_NAME
import com.leobenkel.vibe.core.Schemas.Traits.{SchemaBase, TableRef}
import com.leobenkel.vibe.core.Utils.IdGenerator
import zio.ZIO
import zio.clock.Clock
import zio.random.Random

case class Tag(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  name:              String
) extends SchemaBase[Tag, ID] {
  override def update(updateTimestamp: Date): Tag = {
    this.update(updateTimestamp = updateTimestamp)
  }
}

object Tag extends TableRef[ID, Tag] {
  override def getTableName: TABLE_NAME = "tags"

  override def queryOne(id: ID): QueryZIO[Option[Tag]] = ???

  override def querySeveral(id: Set[ID]): QueryZIO[Seq[Tag]] = ???

  override def querySpecific(whereClause: WHERE_CLAUSE[Tag]): QueryZIO[Seq[Tag]] = ???

  override def deleteRow(id: ID): QueryZIO[Boolean] = ???

  override def insert(row: Tag): QueryZIO[Boolean] = ???

  def apply(name: String): ZIO[Any with Clock with Random, Nothing, Tag] =
    IdGenerator.generateId(name).map {
      case (id, date) =>
        Tag(
          id = id,
          creationTimestamp = date,
          updateTimestamp = date,
          name = name
        )
    }

}
