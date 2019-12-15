package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Schemas._
import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase._
import com.leobenkel.vibe.core.Schemas.Traits.TableRef.TABLE_NAME
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.IdGenerator
import zio.ZIO
import zio.clock.Clock

trait SchemaBase[SELF, PRIMARY_KEY] {
  def update(updateTimestamp: Date): SELF

  type PK = PRIMARY_KEY
  def id:                PRIMARY_KEY
  def creationTimestamp: Date
  //noinspection MutatorLikeMethodIsParameterless
  def updateTimestamp: Date

  final def refreshTimestamp: ZIO[Any with Clock, Nothing, SELF] =
    IdGenerator.getNowTime.map(ts => this.update(updateTimestamp = ts))
}

object SchemaBase {
  type ID = Long
  type Date = Long
  type WHERE_CLAUSE[A] = String

  type QueryZIO[A] = ZIO[Any with Database, Throwable, A]
}

trait TableRef[PRIMARY_KEY, ROW_TYPE <: SchemaBase[ROW_TYPE, PRIMARY_KEY]] {
  type PK = PRIMARY_KEY
  def getTableName: TABLE_NAME
  final def getId(row:           ROW_TYPE): PK = row.id
  def queryOne(id:               PK): QueryZIO[Option[ROW_TYPE]]
  def querySeveral(id:           Set[PK]): QueryZIO[Seq[ROW_TYPE]]
  def querySpecific(whereClause: WHERE_CLAUSE[ROW_TYPE]): QueryZIO[Seq[ROW_TYPE]]
  def deleteRow(id:              PK): QueryZIO[Boolean]
  def insert(row:                ROW_TYPE): QueryZIO[Boolean]
}

object TableRef {
  type TABLE_NAME = String

  val AllTables: Seq[TableRef[_, _]] = Seq(
    Comment,
    Idea,
    JobTitle,
    Tag,
    User,
    UserVotes
  )

  def apply(name: TABLE_NAME): Option[TableRef[_, _]] =
    AllTables.find(_.getTableName.toLowerCase == name.toLowerCase)
}
