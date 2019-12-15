package com.leobenkel.vibe.core.Schemas.Collections

import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase.QueryZIO
import com.leobenkel.vibe.core.Schemas.Traits.Votable
import com.leobenkel.vibe.core.Schemas.{User, UserVotes}
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.VoteValue
import zio.ZIO
import zio.clock.Clock

case class AllVotes(votes: Set[UserVotes]) {
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

//  def find(
//    user:    User,
//    votable: Votable
//  ): Option[UserVotes] =
//    votes.find(_.isSpecificOne(user, votable))

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
    this.voteUp.map(_.find(_.isTheVoter(user)))

  def hasVotedDown(
    user:    User,
    votable: Votable
  ): ZIO[Any, RuntimeException, Option[UserVotes]] =
    this.voteDown.map(_.find(_.isTheVoter(user)))

  def voteUpBy(
    user:    User,
    votable: Votable
  ): ZIO[Any with Clock, RuntimeException, Seq[AllVotes.Operation]] =
    for {
      unVoteOp <- unVote(user, votable)
      newVote  <- UserVotes(user, votable, VoteValue.VoteUp)
    } yield {
      unVoteOp :+ AllVotes.Add(newVote)
    }

  def voteDownBy(
    user:    User,
    votable: Votable
  ): ZIO[Any with Clock, RuntimeException, Seq[AllVotes.Operation]] =
    for {
      unVoteOp <- unVote(user, votable)
      newVote  <- UserVotes(user, votable, VoteValue.VoteDown)
    } yield {
      unVoteOp :+ AllVotes.Add(newVote)
    }

  def unVote(
    user:    User,
    votable: Votable
  ): ZIO[Any, RuntimeException, Seq[AllVotes.Operation]] =
    this.hasVoted(user, votable).map {
      case None               => Seq.empty[AllVotes.Operation]
      case Some(previousVote) => Seq(AllVotes.Delete(previousVote))
    }
}

object AllVotes {
  def execute(operations: Seq[Operation]): ZIO[Any with Database, Throwable, Boolean] = {
    ZIO
      .sequence(operations.map {
        case Add(vote)    => UserVotes.insert(vote)
        case Delete(vote) => UserVotes.deleteRow(vote.id)
      }).map(_.reduce(_ || _))
  }

  def fetch(votable: Votable): QueryZIO[AllVotes] = {
    UserVotes
      .querySpecific(
        s"${UserVotes.getTableName}.attachedToId = ${votable.id} " +
          s"AND ${UserVotes.getTableName}.attachedToTable = ${votable.getTableName}"
      ).map(r => AllVotes(r.toSet))
  }

  sealed trait Operation {
    def vote: UserVotes
    def act:  QueryZIO[Boolean]
    def name: String

    override def toString: String = super.toString
  }

  case class Add(vote: UserVotes) extends Operation {
    override def act: QueryZIO[Boolean] = UserVotes.insert(vote)
    override def name: String = "ADD"
  }

  case class Delete(vote: UserVotes) extends Operation {
    override def act: QueryZIO[Boolean] = UserVotes.deleteRow(vote.id)
    override def name: String = "DELETE"
  }
}
