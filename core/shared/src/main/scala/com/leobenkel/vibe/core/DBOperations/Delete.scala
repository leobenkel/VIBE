package com.leobenkel.vibe.core.DBOperations

import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME

case class Delete[PK](
  tableName: TABLE_NAME,
  id:        PK
) extends OperationNoReturn {
  lazy final override val name:                              String = "DELETE"
  lazy final override protected val displayExtraInformation: String = s"ID:$id"
}
