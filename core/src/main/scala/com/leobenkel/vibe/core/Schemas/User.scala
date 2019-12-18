package com.leobenkel.vibe.core.Schemas

import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Schemas.User.OAuth
import com.leobenkel.vibe.core.Utils.SchemaTypes._
import com.leobenkel.vibe.core.Utils.{IdGenerator, SchemaTypes}
import zio.ZIO
import zio.clock.Clock
import zio.random.Random

case class User(
  id:                ID,
  creationTimestamp: Date,
  updateTimestamp:   Date,
  name:              String,
  email:             String,
  oauthToken:        OAuth,
  skills:            Set[Skill.PK]
) extends SchemaBase[ID] with Updatable[ID, User] {
  lazy final override val toString: String = s"User(ID:$id, N:$name, E:$email, S:${skills.size})"

  lazy final override val get:          User = this
  lazy final override val getTableTool: TableRef[PK, User] = User

  lazy final val allIdeas: QueryZIO[Seq[Idea]] =
    Idea.querySpecific(_.authorId == this.id)
//      s"${Idea.getTableName}.author = ${this.id}")

  lazy final val allEnrolled: QueryZIO[Seq[Idea]] =
    Idea.querySpecific(_.enrolledUserIds.contains(this.id))
//      s"contains(${Idea.getTableName}.enrolled, ${this.id})")

  override def update(updateTimestamp: Date): User = {
    this.update(updateTimestamp = updateTimestamp)
  }
}

object User extends TableRef[ID, User] {
  type OAuth = String

  override def getTableName: TABLE_NAME = "users"

  def apply(
    name:       String,
    email:      String,
    oauthToken: OAuth,
    skills:     Set[Skill]
  ): ZIO[Any with Clock with Random, Nothing, User] =
    IdGenerator.generateId((name, email, oauthToken, skills.map(_.id).mkString(", "))).map {
      case (id, date) =>
        User(id, date, date, name, email, oauthToken, skills.map(_.id))
    }

  override def idFromString(s: String): Comment.PK = SchemaTypes.idFromString(s)
}
