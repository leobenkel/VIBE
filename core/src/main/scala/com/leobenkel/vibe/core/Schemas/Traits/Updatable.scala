package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Utils._
import zio.ZIO
import zio.clock.Clock
import zio.console.Console

trait Updatable[PRIMARY_KEY, SELF <: SchemaBase[PRIMARY_KEY] with Updatable[PRIMARY_KEY, SELF]] {
  private type PK = PRIMARY_KEY
  def get:      SELF
  def isUnique: WHERE_CLAUSE[SELF]

  final def alreadyExists: QueryZIO[Option[SELF]] =
    getTableTool.querySpecific(isUnique).map(_.headOption)

  def update(updateTimestamp: Date): SELF
  def getTableTool: TableRef[PK, SELF]
  private def addNew(): QueryZIO[SELF] = getTableTool.insert(this.get)
  private def update(): QueryZIO[SELF] = getTableTool.update(this.get)
  def restore: QueryZIO[SELF] = getTableTool.queryOne(this.get.id).map {
    case None           => this.get
    case Some(cleanRow) => cleanRow
  }

  final def save(): ZIO[Any with Database with Console, Throwable, SELF] = {
    (for {
      sameExists <- alreadyExists.map(_.isDefined)
      thisExists <- getTableTool.queryOne(this.get.id).map(_.isDefined)
    } yield {
      if (sameExists) {
        ZIO.fail(
          DatabaseException(
            s"A record with incompatible similitude exists for '${this.get.toString}'"
          )
        )
      } else {
        if (thisExists) {
          update()
        } else {
          addNew()
        }
      }
    }).flatten
  }

  final def refreshTimestamp: ZIO[Any with Clock, Nothing, SELF] =
    IdGenerator.getNowTime.map(ts => this.update(updateTimestamp = ts))
}
