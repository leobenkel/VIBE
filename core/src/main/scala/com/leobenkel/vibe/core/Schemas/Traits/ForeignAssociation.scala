package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME

trait ForeignAssociation[PRIMARY_KEY] extends SchemaBase[PRIMARY_KEY] {
  def getTableName: TABLE_NAME
  def id:           PRIMARY_KEY
}
