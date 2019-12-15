package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.{IdGenerator, VoteValue}
import zio._
import zio.clock.Clock

case class UserVotes(
  creationTimestamp: Date,
  updateTimestamp:   Date,
  userId:            User.PK,
  attachedToId:      Votable.FOREIGN_ID,
  attachedToTable:   Votable.FOREIGN_TABLE,
  vote:              Int
) extends SchemaBase[(User.PK, Votable.FOREIGN_ID, Votable.FOREIGN_TABLE)]
    with Updatable[UserVotes] {
  @transient lazy val realVote: IO[RuntimeException, VoteValue] = VoteValue.parse(vote)
  @transient lazy val user:     QueryZIO[Option[User]] = User.queryOne(userId)

  lazy final val getParent: ZIO[Any with Database, Any, Option[Votable]] = {
    ZIO
      .fromOption(
        TableRef(attachedToTable)
          .map(_.asInstanceOf[TableRef[Votable.FOREIGN_ID, Votable]])
          .map(_.queryOne(attachedToId))
      ).flatten
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

  override def queryOne(id: this.PK): QueryZIO[Option[UserVotes]] = ???

  override def querySeveral(id: Set[this.PK]): QueryZIO[Seq[UserVotes]] = ???

  override def querySpecific(whereClause: WHERE_CLAUSE[UserVotes]): QueryZIO[Seq[UserVotes]] = ???

  override def deleteRow(id: this.PK): QueryZIO[Boolean] = ???

  override def insert(row: UserVotes): QueryZIO[Boolean] = ???

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
