package com.leobenkel.vibe.core.Services

import zio.{Task, ZIO}

trait Database {
  def database: Database.Service
}

object Database {
  type QueryType = String
  trait Service {
    def connectionParameter: ConnectionParameter
    def runQuery[A](query: QueryType): Task[Seq[A]]
    def insert[A](
      tableName: String,
      elements:  Seq[A]
    ): Task[Boolean]
  }

  trait ConnectionParameter

  def runQuery[A](queryType: QueryType): ZIO[Database, Throwable, Seq[A]] = {
    ZIO.accessM[Database](_.database.runQuery[A](queryType))
  }
}
