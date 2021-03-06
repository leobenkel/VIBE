package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Utils.{IdGenerator, SchemaTypes}
import zio.ZIO
import zio.clock.Clock
import zio.random.Random

case class Tag(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  name:              String,
  isVisible:         Boolean
) extends SchemaBase[Tag.PK] with Updatable[Tag.PK, Tag] with SchemaT[Tag.PK, Tag] {
  override def update(updateTimestamp: Date): Tag = {
    this.update(updateTimestamp = updateTimestamp)
  }

  lazy final override val get:          Tag = this
  lazy final override val getTableTool: TableRef[PK, Tag] = Tag
  lazy final override val isUnique:     WHERE_CLAUSE[Tag] = (t: Tag) => { t.name == this.name }
}

object Tag extends TableRef[ID, Tag] {
  final override def getTableName: TABLE_NAME = "tags"

  def apply(
    name:    String,
    visible: Boolean
  ): ZIO[Any with Clock with Random, Nothing, Tag] =
    IdGenerator.generateId((name, visible)).map {
      case (id, date) =>
        Tag(
          id = id,
          creationTimestamp = date,
          updateTimestamp = date,
          name = name,
          isVisible = visible
        )
    }

  override def idFromString(s: String): Comment.PK = SchemaTypes.idFromString(s)

  final override def getHeaderColumns: Array[Symbol] = Array(
    'id, 'name, 'isVisible, 'creationDate, 'updateDate
  )

  override def getTableValues(obj: Tag): Array[Any] = {
    Array(
      obj.id,
      obj.name,
      obj.isVisible,
      obj.creationTimestamp,
      obj.updateTimestamp
    )
  }
}
