package com.leobenkel.vibe.core.Schemas.Collections

import com.leobenkel.vibe.core.DBOperations
import com.leobenkel.vibe.core.DBOperations.OperationNoReturn
import com.leobenkel.vibe.core.Schemas.Traits.Votable
import com.leobenkel.vibe.core.Schemas.{User, UserVotes}
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Utils.VoteValue
import zio.ZIO
import zio.clock.Clock
import zio.console.Console

case class AllVotes(votes: Set[UserVotes]) {
  lazy final override val toString: String = s"AllVote(${votes.mkString(";")})"
  @transient lazy final val size:   Int = this.votes.size
  @transient lazy final val length: Int = this.size

  @transient lazy private val voteUp: ZIO[Any, RuntimeException, Set[UserVotes]] =
    ZIO.sequence(votes.map(v => v.isVoteUp.map(r => (r, v)))).map(_.filter(_._1).map(_._2).toSet)

  @transient lazy private val voteDown: ZIO[Any, RuntimeException, Set[UserVotes]] =
    ZIO.sequence(votes.map(v => v.isVoteDown.map(r => (r, v)))).map(_.filter(_._1).map(_._2).toSet)

  @transient lazy val score: ZIO[Any, RuntimeException, Int] = for {
    vUp   <- voteUp
    vDown <- voteDown
  } yield { vUp.size - vDown.size }

  def hasVoted(
    user:    User,
    votable: Votable
  ): ZIO[Any, RuntimeException, Option[UserVotes]] =
    for {
      up   <- hasVotedUp(user, votable)
      down <- hasVotedDown(user, votable)
    } yield {
      up orElse down
    }

  def hasVotedUp(
    user:    User,
    votable: Votable
  ): ZIO[Any, RuntimeException, Option[UserVotes]] =
    this.voteUp.map(_.find(_.isSpecificOne(user, votable)))

  def hasVotedDown(
    user:    User,
    votable: Votable
  ): ZIO[Any, RuntimeException, Option[UserVotes]] =
    this.voteDown.map(_.find(_.isSpecificOne(user, votable)))
}

object AllVotes {
  def voteUpBy(
    user:    User,
    votable: Votable
  ): ZIO[Any with Clock, Nothing, Seq[OperationNoReturn]] =
    for {
      newVote <- UserVotes(user, votable, VoteValue.VoteUp)
    } yield {
      unVote(user, votable) :+ UserVotes.makeInsert(newVote)
    }

  def voteDownBy(
    user:    User,
    votable: Votable
  ): ZIO[Any with Clock, Nothing, Seq[OperationNoReturn]] =
    for {
      newVote <- UserVotes(user, votable, VoteValue.VoteDown)
    } yield {
      unVote(user, votable) :+ UserVotes.makeInsert(newVote)
    }

  def unVote(
    user:    User,
    votable: Votable
  ): Seq[DBOperations.Delete[(User.PK, Votable.FOREIGN_ID, Votable.FOREIGN_TABLE)]] =
    Seq(UserVotes.makeDeleteRow(user, votable))

  def execute(
    operations: Seq[DBOperations.OperationNoReturn]
  ): ZIO[Any with Database with Console, Throwable, Boolean] = {
    ZIO.sequence(operations.map(_.act)).map(_.reduce(_ || _))
  }

  def fetch(votable: Votable): QueryZIO[AllVotes] = {
    UserVotes
      .querySpecific(
        v => votable.isSameVotable(v.attachedToId, v.attachedToTable)
//        s"${UserVotes.getTableName}.attachedToId = ${votable.id} " +
//          s"AND ${UserVotes.getTableName}.attachedToTable = ${votable.getTableName}"
      ).map(r => AllVotes(r.toSet))
  }
}
