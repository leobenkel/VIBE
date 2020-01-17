package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME

case class QueryOne[PK, A <: SchemaBase[PK]](
  tableName: TABLE_NAME,
  id:        PK
) extends Operation[Option[A]] {
  lazy final override val name:                              String = "QUERY_ONE"
  lazy final override protected val displayExtraInformation: String = s"ID:$id"
}
