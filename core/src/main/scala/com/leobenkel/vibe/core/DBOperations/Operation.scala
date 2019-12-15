package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes.QueryZIO

trait Operation[OUTPUT] {
  final def act: QueryZIO[OUTPUT] = Database.runQuery(this)
  def name:      String
  def tableName: String
}

trait OperationNoReturn extends Operation[Boolean]
