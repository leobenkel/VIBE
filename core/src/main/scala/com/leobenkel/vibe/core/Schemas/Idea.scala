package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core
import com.leobenkel.vibe.core.Schemas.Traits.SchemaBase._
import com.leobenkel.vibe.core.Schemas.Traits.TableRef.TABLE_NAME
import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.IdGenerator
import zio.clock.Clock
import zio.random.Random
import zio.{UIO, ZIO}

case class Idea(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  title:             String,
  description:       String,
  authorId:          User.PK,
  enrolledUserIds:   Set[User.PK],
  tagsIds:           Set[Tag.PK],
  commentIds:        Set[Comment.PK]
) extends SchemaBase[Idea, ID] with Commentable with Votable {
  @transient lazy val score: ZIO[Any with Database, Throwable, Int] =
    votes.flatMap(_.score).map(_ + 1)

  @transient lazy val getAuthor: QueryZIO[Option[User]] = User.queryOne(authorId)

  @transient lazy val tags: QueryZIO[Seq[Tag]] = Tag.querySeveral(tagsIds)

  def isAuthor(user: User): ZIO[Any with Database, Throwable, Boolean] =
    this.getAuthor.map(_.exists(_ == user))
  def addTag(tag:         Tag):  Idea = this.copy(tagsIds = this.tagsIds + tag.id)
  def enroll(user:        User): Idea = this.copy(enrolledUserIds = this.enrolledUserIds + user.id)
  def unEnroll(user:      User): Idea = this.copy(enrolledUserIds = this.enrolledUserIds - user.id)
  override def update(ts: Date): Idea = copy(updateTimestamp = ts)
  override def getTableName: TABLE_NAME = Idea.getTableName

  override def voteUpBy(user: User): ZIO[Any with Clock with Database, Throwable, Idea] = {
    isAuthor(user).flatMap {
      case true  => UIO(this)
      case false => super.voteUpBy(user).map(_.asInstanceOf[Idea])
    }
  }

  override def voteDownBy(user: User): ZIO[Any with Clock with Database, Throwable, Idea] = {
    isAuthor(user).flatMap {
      case true  => UIO(this)
      case false => super.voteDownBy(user).map(_.asInstanceOf[Idea])
    }
  }

  override def unVoteDown(user: User): ZIO[Any with Clock with Database, Throwable, Votable] = {
    isAuthor(user).flatMap {
      case true  => UIO(this)
      case false => super.unVoteDown(user).map(_.asInstanceOf[Idea])
    }
  }

  override def unVoteUp(user: User): ZIO[Any with Clock with Database, Throwable, Votable] = {
    isAuthor(user).flatMap {
      case true  => UIO(this)
      case false => super.unVoteUp(user)
    }
  }
}

object Idea extends TableRef[ID, Idea] {
  override def getTableName: TABLE_NAME = "ideas"

  override def queryOne(id: ID): QueryZIO[Option[Idea]] = ???

  override def querySeveral(id: Set[ID]): QueryZIO[Seq[Idea]] = ???

  override def querySpecific(whereClause: WHERE_CLAUSE[Idea]): QueryZIO[Seq[Idea]] = ???

  override def deleteRow(id: core.Schemas.Idea.PK): QueryZIO[Boolean] = ???

  override def insert(row: Idea): QueryZIO[Boolean] = ???

  def apply(
    title:       String,
    description: String,
    author:      User,
    tagsIds:     Set[Tag]
  ): ZIO[Any with Clock with Random, Nothing, Idea] = {
    IdGenerator.generateId((title, description, author.id, tagsIds.map(_.id), Set.empty)).map {
      case (id, date) =>
        new Idea(
          id = id,
          creationTimestamp = date,
          updateTimestamp = date,
          title = title,
          description = description,
          authorId = author.id,
          enrolledUserIds = Set.empty,
          tagsIds = tagsIds.map(_.id),
          commentIds = Set.empty
        )
    }
  }

}
