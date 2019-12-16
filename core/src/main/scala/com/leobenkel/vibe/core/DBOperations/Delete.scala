package com.leobenkel.vibe.core.DBOperations
import com.leobenkel.vibe.core.Utils.Log
import zio.ZIO
import zio.console.Console

case class Delete[PK](
  tableName: String,
  id:        PK
) extends OperationNoReturn {
  lazy final override val name:                    String = "DELETE"
  lazy final override val displayExtraInformation: String = s"ID:$id"
}
