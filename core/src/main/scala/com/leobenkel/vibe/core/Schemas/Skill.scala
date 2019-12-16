package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Utils.IdGenerator
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import zio.ZIO
import zio.clock.Clock
import zio.random.Random

case class Skill(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  name:              String
) extends SchemaBase[ID] with Updatable[ID, Skill] {
  lazy final override val get:          Skill = this
  lazy final override val getTableTool: TableRef[PK, Skill] = Skill

  override def update(updateTimestamp: Date): Skill = {
    this.update(updateTimestamp = updateTimestamp)
  }
}

object Skill extends TableRef[ID, Skill] {
  override def getTableName: TABLE_NAME = "job_titles"

  def apply(name: String): ZIO[Any with Clock with Random, Nothing, Skill] =
    IdGenerator.generateId(name).map {
      case (id, date) =>
        Skill(
          id = id,
          creationTimestamp = date,
          updateTimestamp = date,
          name = name
        )
    }

}
