package com.leobenkel.vibe.core.Services

import com.leobenkel.vibe.core.DBOperations.Operation
import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase
import com.leobenkel.vibe.core.Services.Database.QueryType
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import zio.{Task, UIO, ZIO}

import scala.collection.mutable

trait Database {
  def database: Database.Service
}

object Database {
  type QueryType[A] = Operation[A]
  trait Service {
    def connectionParameter: ConnectionParameter

    def runQuery[OUT](query: QueryType[OUT]): Task[OUT]
  }

  trait ConnectionParameter

  def runQuery[OUT](queryType: QueryType[OUT]): QueryZIO[OUT] = {
    ZIO.accessM[Database](_.database.runQuery[OUT](queryType))
  }
}

object DatabaseInMemory extends Database.Service {
  import scala.collection.mutable.Map

  override def connectionParameter: Database.ConnectionParameter = ???

  val DB: mutable.Map[TABLE_NAME, mutable.Map[Any, SchemaBase[_]]] = mutable.Map.empty

  override def runQuery[OUT](query: QueryType[OUT]): Task[OUT] = Task {
    import com.leobenkel.vibe.core.DBOperations._
    query match {
      case Delete(tableName, id) =>
        println(s"DELETE $tableName - $id")
        DB.getOrElse(tableName, mutable.Map.empty).remove(id)
        true.asInstanceOf[OUT]
      case Insert(tableName, row) =>
        println(s"INSERT $tableName - $row")
        val newInnerDB = DB.getOrElse(tableName, mutable.Map.empty)
        newInnerDB.update(row.id, row)
        DB.update(tableName, newInnerDB)
        true.asInstanceOf[OUT]
      case QueryOne(tableName, id) =>
        println(s"QUERY $tableName - $id")
        DB.getOrElse(tableName, mutable.Map.empty).get(id).asInstanceOf[OUT]
      case QuerySeveralOnID(tableName, ids) =>
        println(s"QUERY $tableName - ${ids.mkString(", ")}")
        DB.getOrElse(tableName, mutable.Map.empty)
          .filterKeys(k => ids.contains(k)).asInstanceOf[OUT]
      case QueryWhereClause(tableName, whereClause) =>
        println(s"QUERY $tableName")
        DB.getOrElse(tableName, mutable.Map.empty)
          .filter { case (_, row) => whereClause(row) }.values.toSeq.asInstanceOf[OUT]
    }
  }
}
