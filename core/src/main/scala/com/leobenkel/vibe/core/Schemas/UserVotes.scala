package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.DBOperations.Delete
import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Utils.{IdGenerator, VoteValue}
import zio._
import zio.clock.Clock
import zio.console.Console

case class UserVotes(
  creationTimestamp: Date,
  updateTimestamp:   Date,
  userId:            User.PK,
  attachedToId:      Votable.FOREIGN_ID,
  attachedToTable:   Votable.FOREIGN_TABLE,
  vote:              Int
) extends SchemaBase[(User.PK, Votable.FOREIGN_ID, Votable.FOREIGN_TABLE)]
    with Updatable[(User.PK, Votable.FOREIGN_ID, Votable.FOREIGN_TABLE), UserVotes] {
  lazy final override val get:          UserVotes = this
  lazy final override val getTableTool: TableRef[PK, UserVotes] = UserVotes
  @transient lazy val realVote:         IO[RuntimeException, VoteValue] = VoteValue.parse(vote)
  lazy final val user:                  QueryZIO[Option[User]] = User.queryOne(userId)
  lazy final override val toString: String = {
    s"Vote(u:$userId, t:($attachedToTable,$attachedToId), v:$vote)"
  }

  lazy final val getParent: ZIO[Any with Database with Console, Throwable, Option[Votable]] = {
    TableRef[Votable.FOREIGN_ID, Votable](attachedToTable)
      .flatMap(_.queryOne(attachedToId))
  }

  def isTheVoter(user:    User): Boolean = this.userId == user.id
  def isTheTarget(target: Votable): Boolean =
    target.isSameVotable(attachedToId, attachedToTable)
  def isSpecificOne(
    user:   User,
    target: Votable
  ): Boolean = isTheVoter(user) && isTheTarget(target)

  @transient lazy final val isVoteUp:   IO[RuntimeException, Boolean] = realVote.map(_.isUp)
  @transient lazy final val isVoteDown: IO[RuntimeException, Boolean] = isVoteUp.map(!_)

  override def id: (User.PK, Votable.FOREIGN_ID, Votable.FOREIGN_TABLE) =
    (userId, attachedToId, attachedToTable)

  override def update(updateTimestamp: Date): UserVotes = {
    this.update(updateTimestamp = updateTimestamp)
  }
}

object UserVotes extends TableRef[(User.PK, Votable.FOREIGN_ID, Votable.FOREIGN_TABLE), UserVotes] {
  override def getTableName: TABLE_NAME = "user_votes"

  def makePk(
    user:    User,
    votable: Votable
  ): PK = (
    user.id,
    votable.id,
    votable.getTableName
  )

  final def makeDeleteRow(
    user:    User,
    votable: Votable
  ): Delete[PK] = {
    makeDeleteRow(UserVotes.makePk(user, votable))
  }

  def apply(
    user:    User,
    votable: Votable,
    vote:    VoteValue
  ): ZIO[Any with Clock, Nothing, UserVotes] = {
    for {
      date <- IdGenerator.getNowTime
    } yield {
      UserVotes(
        creationTimestamp = date,
        updateTimestamp = date,
        userId = user.id,
        attachedToId = votable.id,
        attachedToTable = votable.getTableName,
        vote = vote.encoding
      )
    }
  }

}
