package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Utils.IdGenerator
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import zio.ZIO
import zio.clock.Clock
import zio.random.Random

case class Tag(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  name:              String
) extends SchemaBase[ID] with Updatable[ID, Tag] {
  override def update(updateTimestamp: Date): Tag = {
    this.update(updateTimestamp = updateTimestamp)
  }

  lazy final override val get:          Tag = this
  lazy final override val getTableTool: TableRef[PK, Tag] = Tag
}

object Tag extends TableRef[ID, Tag] {
  override def getTableName: TABLE_NAME = "tags"

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
