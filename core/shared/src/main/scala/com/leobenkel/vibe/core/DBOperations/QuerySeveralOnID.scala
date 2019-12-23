package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME

case class QuerySeveralOnID[PK, A <: SchemaBase[PK]](
  tableName: TABLE_NAME,
  ids:       Set[PK]
) extends Operation[Seq[A]] {
  lazy final override val name:                    String = "QUERY_SEVERAL_IDS"
  protected lazy final override val displayExtraInformation: String = s"IDs:${ids.mkString(", ")}"
}
