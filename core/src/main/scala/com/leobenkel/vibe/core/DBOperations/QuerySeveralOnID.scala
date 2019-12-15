package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase

case class QuerySeveralOnID[PK, A <: SchemaBase[PK]](
  tableName: String,
  ids:       Set[PK]
) extends Operation[Seq[A]] {
  override def name: String = "QUERY_SEVERAL_IDS"
}
