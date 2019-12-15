package com.leobenkel.vibe.core.DBOperations
import com.leobenkel.vibe.core.Schemas.Traits.{Insertable, SchemaBase}

case class Insert[A <: SchemaBase[_]](
  tableName: String,
  row:       A
) extends OperationNoReturn {
  lazy final override val name: String = "INSERT"
}
