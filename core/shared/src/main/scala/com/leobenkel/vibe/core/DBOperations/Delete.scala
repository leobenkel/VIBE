package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME

case class Delete[PK](
  tableName: TABLE_NAME,
  id:        PK
) extends OperationNoReturn {
  lazy final override val name:                    String = "DELETE"
  protected lazy final override val displayExtraInformation: String = s"ID:$id"
}
