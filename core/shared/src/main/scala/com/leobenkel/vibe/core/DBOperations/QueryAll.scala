package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME

case class QueryAll[PK, A <: SchemaBase[PK]](tableName: TABLE_NAME) extends Operation[Seq[A]] {
  lazy final override val name:                              String = "QUERY_ALL"
  lazy final override protected val displayExtraInformation: String = s"all"
}
