package com.leobenkel.vibe.core.DBOperations
import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME

case class Insert[A <: SchemaBase[_]](
  tableName: TABLE_NAME,
  row:       A
) extends OperationNoReturn {
  lazy final override val name:                    String = "INSERT"
  protected lazy final override val displayExtraInformation: String = s"R:${row.toString}"
}
