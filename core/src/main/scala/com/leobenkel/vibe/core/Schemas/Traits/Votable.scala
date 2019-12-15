package com.leobenkel.vibe.core.Schemas.Traits

import SchemaBase.{ID, QueryZIO}
import com.leobenkel.vibe.core.Schemas.Collections.AllVotes
import com.leobenkel.vibe.core.Schemas._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.{DatabaseException, VoteValue}
import zio.ZIO
import zio.clock.Clock

trait Votable extends ForeignAssociation[Votable.FOREIGN_ID] {
  private type PRIMARY_KEY = Votable.FOREIGN_ID
  def voteIds: Set[UserVotes.PK]
  @transient lazy final val votes: QueryZIO[AllVotes] = AllVotes.fetch(this)

  def isSameVotable(other: Votable): Boolean = {
    this.isSameVotable(other.id, other.getTableName)
  }

  def isSameVotable(
    otherId:    PRIMARY_KEY,
    otherTable: TableRef.TABLE_NAME
  ): Boolean = {
    this.id == otherId && this.getTableName == otherTable
  }

 def refreshTimestamp: ZIO[Any with Clock, Nothing, Votable]

  def executeOperations(operations : Seq[AllVotes.Operation]) = {
    AllVotes.execute(operations).map{
      case true => this.refreshTimestamp
      case false => ZIO.fail(DatabaseException(s"One or more of the following operations have failed: ${operations.mkString(", ")}") )
    }
  }

  def voteUpBy(user: User): ZIO[Any with Clock with Database, Throwable, Seq[AllVotes.Operation]] = {
    this.votes.flatMap(_.voteUpBy(user, this))
  }

  def voteDownBy(user: User): Idea = {
    if (isAuthor(user)) {
      this
    } else {
      this.copy(votes = this.votes.voteDownBy(user))
    }
  }

  def unVoteUp(user: User): Idea = {
    if (isAuthor(user)) {
      this
    } else {
      this.copy(votes = this.votes.unVoteUp(user))
    }
  }

  def unVoteDown(user: User): Idea = {
    if (authorIds == user) {
      this
    } else {
      this.copy(votes = this.votes.unVoteDown(user))
    }
  }
}
object Votable {
  type FOREIGN_ID = ID
  type FOREIGN_TABLE = TableRef.TABLE_NAME
}
