package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Schemas.Traits.{SchemaBase, TableRef, Updatable}
import com.leobenkel.vibe.core.Utils.IdGenerator
import zio.ZIO
import zio.clock.Clock
import zio.random.Random

case class Skill(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  name:              String
) extends SchemaBase[ID] with Updatable[Skill] {
  override def update(updateTimestamp: Date): Skill = {
    this.update(updateTimestamp = updateTimestamp)
  }
}

object Skill extends TableRef[ID, Skill] {
  override def getTableName: TABLE_NAME = "job_titles"

  override def queryOne(id: ID): QueryZIO[Option[Skill]] = ???

  override def querySeveral(id: Set[ID]): QueryZIO[Seq[Skill]] = ???

  override def querySpecific(whereClause: WHERE_CLAUSE[Skill]): QueryZIO[Seq[Skill]] = ???

  override def deleteRow(id: ID): QueryZIO[Boolean] = ???

  override def insert(row: Skill): QueryZIO[Boolean] = ???

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
