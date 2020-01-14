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
) extends SchemaBase[User.PK] with Updatable[User.PK, User] with SchemaT[User.PK, User] {
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

  lazy final override val isUnique: WHERE_CLAUSE[User] = (u: User) => { u.email == this.email }
}

object User extends TableRef[ID, User] {
  type OAuth = String

  override final def getTableName: TABLE_NAME = "users"

 override final def getHeaderColumns: Array[Symbol] = ???

  override final def getTableValues(obj: User): Array[Any] = ???

  def apply(
    name:       String,
    email:      String,
    oauthToken: OAuth,
    skills:     Set[Skill.PK]
  ): ZIO[Any with Clock with Random, Nothing, User] =
    IdGenerator.generateId((name, email, oauthToken, skills.mkString(", "))).map {
      case (id, date) =>
        User(
          id = id,
          creationTimestamp = date,
          updateTimestamp = date,
          name = name,
          email = email,
          oauthToken = oauthToken,
          skills = skills
        )
    }

  def make(
    name:       String,
    email:      String,
    oauthToken: OAuth,
    skills:     Set[Skill]
  ): ZIO[Any with Clock with Random, Nothing, User] =
    apply(name, email, oauthToken, skills.map(_.id))

  override def idFromString(s: String): Comment.PK = SchemaTypes.idFromString(s)
}
