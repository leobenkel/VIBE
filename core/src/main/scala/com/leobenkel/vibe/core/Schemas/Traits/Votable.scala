package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Schemas.Collections.AllVotes
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Schemas._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.DatabaseException
import zio.ZIO
import zio.clock.Clock

trait Votable extends ForeignAssociation[Votable.FOREIGN_ID] {
  private type PRIMARY_KEY = Votable.FOREIGN_ID

  @transient lazy final val votes: QueryZIO[AllVotes] = AllVotes.fetch(this)

  def isSameVotable(other: Votable): Boolean = {
    this.isSameVotable(other.id, other.getTableName)
  }

  def isSameVotable(
    otherId:    PRIMARY_KEY,
    otherTable: TABLE_NAME
  ): Boolean = {
    this.id == otherId && this.getTableName == otherTable
  }

  def refreshTimestamp: ZIO[Any with Clock, Nothing, Votable]

  private def executeOperations(
    operations: Seq[AllVotes.Operation]
  ): ZIO[Any with Clock with Database, Throwable, Votable] = {
    AllVotes.execute(operations).flatMap {
      case true => this.refreshTimestamp
      case false =>
        ZIO.fail(
          DatabaseException(
            s"One or more of the following operations " +
              s"have failed: ${operations.mkString(", ")}"
          )
        )
    }
  }

  def voteUpBy(user: User): ZIO[Any with Clock with Database, Throwable, Votable] = {
    AllVotes
      .voteUpBy(user, this)
      .flatMap(executeOperations)
  }

  def voteDownBy(user: User): ZIO[Any with Clock with Database, Throwable, Votable] = {
    AllVotes
      .voteDownBy(user, this)
      .flatMap(executeOperations)
  }

  def unVoteUp(user: User): ZIO[Any with Clock with Database, Throwable, Votable] = {
    executeOperations(AllVotes.unVote(user, this))
  }

  def unVoteDown(user: User): ZIO[Any with Clock with Database, Throwable, Votable] = {
    executeOperations(AllVotes.unVote(user, this))
  }
}

object Votable {
  type FOREIGN_ID = ID
  type FOREIGN_TABLE = TABLE_NAME
}
