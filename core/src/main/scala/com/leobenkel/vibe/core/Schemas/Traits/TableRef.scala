package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Schemas.{Comment, Idea, Skill, Tag, User, UserVotes}
import com.leobenkel.vibe.core.DBOperations

trait TableRef[PRIMARY_KEY, ROW_TYPE <: SchemaBase[PRIMARY_KEY]] {
  type PK = PRIMARY_KEY
  def getTableName: TABLE_NAME

  final def getId(row: ROW_TYPE): PK = row.id

  final def queryOne(id: PK): QueryZIO[Option[ROW_TYPE]] = {
    DBOperations.QueryOne[PRIMARY_KEY, ROW_TYPE](getTableName, id).act
  }

  final def querySeveral(ids: Set[PK]): QueryZIO[Seq[ROW_TYPE]] = {
    DBOperations.QuerySeveralOnID[PRIMARY_KEY, ROW_TYPE](getTableName, ids).act
  }

  final def querySpecific(whereClause: WHERE_CLAUSE[ROW_TYPE]): QueryZIO[Seq[ROW_TYPE]] = {
    DBOperations.QueryWhereClause[ROW_TYPE](getTableName, whereClause).act
  }

  final def deleteRow(id: PK): QueryZIO[Boolean] = {
    DBOperations.Delete(getTableName, id).act
  }

  final def insert(row: ROW_TYPE): QueryZIO[Boolean] = {
    DBOperations.Insert(getTableName, row).act
  }
}

object TableRef {
  val AllTables: Seq[TableRef[_, _]] = Seq(
    Comment,
    Idea,
    Skill,
    Tag,
    User,
    UserVotes
  )

  def apply(name: TABLE_NAME): Option[TableRef[_, _]] =
    AllTables.find(_.getTableName.toLowerCase == name.toLowerCase)
}
