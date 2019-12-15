package com.leobenkel.vibe.core.Schemas.Traits

trait ForeignAssociation[PRIMARY_KEY]{
  def getTableName: TableRef.TABLE_NAME
  def id:           PRIMARY_KEY
}
