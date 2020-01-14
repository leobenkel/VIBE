package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Utils.{IdGenerator, SchemaTypes}
import zio.clock.Clock
import zio.console.Console
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
) extends SchemaBase[Idea.PK] with Commentable with Votable with Updatable[Idea.PK, Idea]
    with SchemaT[Idea.PK, Idea] {
  lazy final override val toString: String = s"Idea(ID:$id, T:'$title', A:$authorId, " +
    s"E:${enrolledUserIds.size}, T:${tagsIds.size}, C:${commentIds.size})"
  lazy final override val get:          Idea = this
  lazy final override val getTableTool: TableRef[PK, Idea] = Idea
  lazy final val score: ZIO[Any with Database with Console, Throwable, Int] =
    votes.flatMap(_.score).map(_ + 1)

  lazy final override val isUnique: WHERE_CLAUSE[Idea] = (i: Idea) => { i.title == this.title }

  lazy final val getAuthor: QueryZIO[Option[User]] = User.queryOne(authorId)
  lazy final val tags:      QueryZIO[Seq[Tag]] = Tag.querySeveral(tagsIds)
  def isAuthor(user:      User): Boolean = user.id == authorId
  def addTag(tag:         Tag):  Idea = this.copy(tagsIds = this.tagsIds + tag.id)
  def enroll(user:        User): Idea = this.copy(enrolledUserIds = this.enrolledUserIds + user.id)
  def unEnroll(user:      User): Idea = this.copy(enrolledUserIds = this.enrolledUserIds - user.id)
  override def update(ts: Date): Idea = copy(updateTimestamp = ts)
  lazy final override val getTableName: TABLE_NAME = Idea.getTableName

  override def voteUpBy(
    user: User
  ): ZIO[Any with Clock with Database with Console, Throwable, Idea] = {
    if (isAuthor(user)) {
      UIO(this)
    } else {
      super.voteUpBy(user).map(_.asInstanceOf[Idea])
    }
  }

  override def voteDownBy(
    user: User
  ): ZIO[Any with Clock with Database with Console, Throwable, Idea] = {
    if (isAuthor(user)) {
      UIO(this)
    } else {
      super.voteDownBy(user).map(_.asInstanceOf[Idea])
    }
  }

  override def unVoteDown(
    user: User
  ): ZIO[Any with Clock with Database with Console, Throwable, Idea] = {
    if (isAuthor(user)) {
      UIO(this)
    } else {
      super.unVoteDown(user).map(_.asInstanceOf[Idea])
    }
  }

  override def unVoteUp(
    user: User
  ): ZIO[Any with Clock with Database with Console, Throwable, Idea] = {
    if (isAuthor(user)) {
      UIO(this)
    } else {
      super.unVoteUp(user).map(_.asInstanceOf[Idea])
    }
  }
}

object Idea extends TableRef[ID, Idea] {
  override final def getTableName: TABLE_NAME = "ideas"

 override final def getHeaderColumns: Array[Symbol] = ???

  override final def getTableValues(obj: Idea): Array[Any] = ???

  def apply(
    title:       String,
    description: String,
    authorId:    User.PK,
    tagsIds:     Set[Tag.PK]
  ): ZIO[Any with Clock with Random, Nothing, Idea] = {
    IdGenerator.generateId((title, description, authorId, tagsIds, Set.empty)).map {
      case (id, date) =>
        Idea(
          id = id,
          creationTimestamp = date,
          updateTimestamp = date,
          title = title,
          description = description,
          authorId = authorId,
          enrolledUserIds = Set.empty,
          tagsIds = tagsIds,
          commentIds = Set.empty
        )
    }
  }

  def apply(
    title:       String,
    description: String,
    author:      User,
    tags:        Set[Tag]
  ): ZIO[Any with Clock with Random, Nothing, Idea] = {
    apply(title, description, author.id, tags.map(_.id))
  }

  override def idFromString(s: String): Comment.PK = SchemaTypes.idFromString(s)
}
