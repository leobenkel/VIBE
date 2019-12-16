package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase

case class QuerySeveralOnID[PK, A <: SchemaBase[PK]](
  tableName: String,
  ids:       Set[PK]
) extends Operation[Seq[A]] {
  lazy final override val name:                    String = "QUERY_SEVERAL_IDS"
  lazy final override val displayExtraInformation: String = s"IDs:${ids.mkString(", ")}"
}
