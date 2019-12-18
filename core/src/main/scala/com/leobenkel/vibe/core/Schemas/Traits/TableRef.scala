package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.DBOperations
import com.leobenkel.vibe.core.DBOperations._
import com.leobenkel.vibe.core.Schemas._
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import zio.{Task, ZIO}

import scala.util.Try

trait TableRef[PRIMARY_KEY, ROW_TYPE <: SchemaBase[PRIMARY_KEY]] {
  type PK = PRIMARY_KEY

  def getTableName: TABLE_NAME

  def idFromString(s: String): PK

  final def getId(row: ROW_TYPE): PK = row.id

  final def makeQueryOne(id: PK): QueryOne[PRIMARY_KEY, ROW_TYPE] =
    DBOperations.QueryOne[PRIMARY_KEY, ROW_TYPE](getTableName, id)

  final def queryOne(id: PK): QueryZIO[Option[ROW_TYPE]] =
    makeQueryOne(id).act

  final def makeQueryAll(): QueryAll[PRIMARY_KEY, ROW_TYPE] =
    DBOperations.QueryAll[PRIMARY_KEY, ROW_TYPE](getTableName)

  final def queryAll(): QueryZIO[Seq[ROW_TYPE]] =
    makeQueryAll().act

  final def makeQuerySeveral(ids: Set[PK]): QuerySeveralOnID[PRIMARY_KEY, ROW_TYPE] =
    DBOperations.QuerySeveralOnID[PRIMARY_KEY, ROW_TYPE](getTableName, ids)

  final def querySeveral(ids: Set[PK]): QueryZIO[Seq[ROW_TYPE]] =
    makeQuerySeveral(ids).act

  final def makeQuerySpecific(whereClause: WHERE_CLAUSE[ROW_TYPE]): QueryWhereClause[ROW_TYPE] =
    DBOperations.QueryWhereClause[ROW_TYPE](getTableName, whereClause)

  final def querySpecific(whereClause: WHERE_CLAUSE[ROW_TYPE]): QueryZIO[Seq[ROW_TYPE]] =
    makeQuerySpecific(whereClause).act

  final def makeDeleteRow(id: PK): Delete[PK] =
    DBOperations.Delete(getTableName, id)

  final def deleteRow(id: PK): QueryZIO[Boolean] =
    makeDeleteRow(id).act

  final def makeInsert(row: ROW_TYPE): Insert[ROW_TYPE] =
    DBOperations.Insert(getTableName, row)

  final def insert(row: ROW_TYPE): QueryZIO[Boolean] =
    makeInsert(row).act
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

  sealed trait TableException extends Throwable

  case class UnknownTableErrorException(error: Throwable)
      extends Exception(s"Unknown error: ${error.toString}") with TableException {
    override def getCause: Throwable = error
  }

  case class UnknownTableNameException(table: TABLE_NAME)
      extends Exception(s"Impossible to find a table named '$table'.") with TableException

  case class ConversionTableTypeException(
    table: TABLE_NAME,
    cause: Throwable
  ) extends Exception(s"Impossible to convert table named '$table' to correct types.")
      with TableException {
    override def getCause: Throwable = cause
  }

  def apply[PK, ROW <: SchemaBase[PK]](
    name: TABLE_NAME
  ): ZIO[Any, TableException, TableRef[PK, ROW]] =
    Task {
      AllTables
        .find(_.getTableName.toLowerCase == name.toLowerCase)
        .map(t => Try(t.asInstanceOf[TableRef[PK, ROW]]))
    }.flatMap {
        case Some(t) =>
          ZIO
            .fromTry(t)
            .mapError(ex => ConversionTableTypeException(name, ex))
        case None => ZIO.fail(UnknownTableNameException(name))
      }.mapError {
        case e: TableException => e
        case ex => UnknownTableErrorException(ex)
      }
}
