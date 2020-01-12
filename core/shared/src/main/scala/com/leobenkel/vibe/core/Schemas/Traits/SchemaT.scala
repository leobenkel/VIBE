package com.leobenkel.vibe.core.Schemas.Traits

trait SchemaT[PRIMARY_KEY, SELF <: SchemaT[PRIMARY_KEY, SELF]]
    extends SchemaBase[PRIMARY_KEY] with Updatable[PRIMARY_KEY, SELF] {}
