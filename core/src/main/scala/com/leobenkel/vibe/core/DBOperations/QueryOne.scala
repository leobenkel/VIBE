package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase

case class QueryOne[PK, A <: SchemaBase[PK]](
  tableName: String,
  id:        PK
) extends Operation[Option[A]] {
  lazy final override val name:                    String = "QUERY_ONE"
  lazy final override val displayExtraInformation: String = s"ID:$id"
}
