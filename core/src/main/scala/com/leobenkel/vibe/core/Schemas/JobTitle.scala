package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase._
import com.leobenkel.vibe.core.Schemas.Traits.TableRef.TABLE_NAME
import com.leobenkel.vibe.core.Schemas.Traits.{SchemaBase, TableRef}
import com.leobenkel.vibe.core.Utils.IdGenerator
import zio.ZIO
import zio.clock.Clock
import zio.random.Random

case class JobTitle(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  name:              String
) extends SchemaBase[JobTitle, ID] {
  override def copy(updateTimestamp: Date): JobTitle = {
    this.copy(updateTimestamp = updateTimestamp)
  }
}

object JobTitle extends TableRef[ID, JobTitle] {
  override def getTableName: TABLE_NAME = "job_titles"

  override def queryOne(id: ID): QueryZIO[Option[JobTitle]] = ???

  override def querySeveral(id: Set[ID]): QueryZIO[Seq[JobTitle]] = ???

  override def querySpecific(whereClause: WHERE_CLAUSE[JobTitle]): QueryZIO[Seq[JobTitle]] = ???

  override def deleteRow(id: ID): QueryZIO[Boolean] = ???

  override def insert(row: JobTitle): QueryZIO[Boolean] = ???

  def apply(name: String): ZIO[Any with Clock with Random, Nothing, JobTitle] =
    IdGenerator.generateId(name).map {
      case (id, date) =>
        JobTitle(
          id = id,
          creationTimestamp = date,
          updateTimestamp = date,
          name = name
        )
    }

}
