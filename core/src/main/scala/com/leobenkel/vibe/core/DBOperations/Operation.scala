package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.Log
import com.leobenkel.vibe.core.Utils.SchemaTypes.QueryZIO
import zio.ZIO
import zio.console.Console

trait Operation[OUTPUT] {
  final def act: QueryZIO[OUTPUT] = Database.runQuery[OUTPUT](this)
  def name:                    String
  def tableName:               String
  def displayExtraInformation: String

  final def display: ZIO[Any with Console, Throwable, this.type] = {
    for {
      _ <- Log(s"[Database][$name] T:$tableName - $displayExtraInformation")
    } yield {
      this
    }
  }
}

trait OperationNoReturn extends Operation[Boolean]
