package com.leobenkel.vibe.core.Services

import com.leobenkel.vibe.core.DBOperations.Operation
import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase
import com.leobenkel.vibe.core.Services.Database.QueryType
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import zio.ZIO

import scala.collection.mutable

trait Database {
  def database: Database.Service
}

object Database {
  type QueryType[A] = Operation[A]
  trait Service {
    def connectionParameter: ConnectionParameter

    def runQuery[OUT](queryType: QueryType[OUT]): QueryZIO[OUT]
  }

  trait ConnectionParameter

  def runQuery[OUT](queryType: QueryType[OUT]): QueryZIO[OUT] = {
    ZIO.environment[Database].flatMap(_.database.runQuery[OUT](queryType))
  }
}

object DatabaseInMemory extends Database.Service {

  override def connectionParameter: Database.ConnectionParameter = ???

  val DB: mutable.Map[TABLE_NAME, mutable.Map[Any, SchemaBase[_]]] = mutable.Map.empty

  def runQuery[OUT](queryType: QueryType[OUT]): QueryZIO[OUT] = {
    queryType.display
      .map { query =>
        import com.leobenkel.vibe.core.DBOperations._
        query match {
          case Delete(tableName, id) =>
            DB.getOrElse(tableName, mutable.Map.empty).remove(id)
            true.asInstanceOf[OUT]
          case Insert(tableName, row) =>
            val newInnerDB = DB.getOrElse(tableName, mutable.Map.empty)
            newInnerDB.update(row.id, row)
            DB.update(tableName, newInnerDB)
            true.asInstanceOf[OUT]
          case QueryOne(tableName, id) =>
            DB.getOrElse(tableName, mutable.Map.empty).get(id).asInstanceOf[OUT]
          case QuerySeveralOnID(tableName, ids) =>
            DB.getOrElse(tableName, mutable.Map.empty)
              .filterKeys(k => ids.contains(k)).asInstanceOf[OUT]
          case QueryWhereClause(tableName, whereClause) =>
            DB.getOrElse(tableName, mutable.Map.empty)
              .filter { case (_, row) => whereClause(row) }.values.toSeq.asInstanceOf[OUT]
        }
      }
  }
}
