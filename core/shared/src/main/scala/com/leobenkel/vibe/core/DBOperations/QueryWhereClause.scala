package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase
import com.leobenkel.vibe.core.Utils.SchemaTypes.{TABLE_NAME, WHERE_CLAUSE}

case class QueryWhereClause[A <: SchemaBase[_]](
  tableName:   TABLE_NAME,
  whereClause: WHERE_CLAUSE[A]
) extends Operation[Seq[A]] {
  lazy final override val name: String = "QUERY_WHERE"
  lazy final override protected val displayExtraInformation: String =
    s"Where:${whereClause.toString}"
}
