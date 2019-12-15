package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Schemas.{Comment, Idea, JobTitle, Tag, User, UserVotes}

trait TableRef[PRIMARY_KEY, ROW_TYPE <: SchemaBase[PRIMARY_KEY]] {
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
