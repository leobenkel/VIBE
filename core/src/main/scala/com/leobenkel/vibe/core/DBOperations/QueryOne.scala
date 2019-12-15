package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase

case class QueryOne[PK, A <: SchemaBase[PK]](
  tableName: String,
  id:        PK
) extends Operation[Option[A]] {
  override def name: String = "QUERY_ONE"
}
