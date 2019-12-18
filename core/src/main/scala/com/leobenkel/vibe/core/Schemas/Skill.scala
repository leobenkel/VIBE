package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Utils.{IdGenerator, SchemaTypes}
import zio.ZIO
import zio.clock.Clock
import zio.random.Random

case class Skill(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  name:              String,
  isVisible:         Boolean
) extends SchemaBase[ID] with Updatable[ID, Skill] {
  lazy final override val get:          Skill = this
  lazy final override val getTableTool: TableRef[PK, Skill] = Skill

  override def update(updateTimestamp: Date): Skill = {
    this.update(updateTimestamp = updateTimestamp)
  }
}

object Skill extends TableRef[ID, Skill] {
  override def getTableName: TABLE_NAME = "skills"

  def apply(
    name:      String,
    isVisible: Boolean
  ): ZIO[Any with Clock with Random, Nothing, Skill] =
    IdGenerator.generateId(name).map {
      case (id, date) =>
        Skill(
          id = id,
          creationTimestamp = date,
          updateTimestamp = date,
          name = name,
          isVisible = isVisible
        )
    }

  override def idFromString(s: String): Comment.PK = SchemaTypes.idFromString(s)
}
