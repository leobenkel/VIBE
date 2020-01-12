package com.leobenkel.vibe.server.Routes.Root

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.leobenkel.vibe.core.Messages.MessageStatus
import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Schemas.User.OAuth
import com.leobenkel.vibe.core.Schemas._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME
import com.leobenkel.vibe.server.Messages._
import com.leobenkel.vibe.server.Routes.Utils.RoutePutSchema.ZCREATE
import com.leobenkel.vibe.server.Routes.Utils.RouteUtils._
import com.leobenkel.vibe.server.Routes.Utils.{RouteTrait, RouteTraitWithChild, _}
import io.circe.Encoder
import io.circe.generic.auto._
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

import scala.reflect.ClassTag

trait ModelRootRoute extends RouteTraitWithChild {
  protected def env: Any with Database with Console with Clock with Random
  private val self: ModelRootRoute = this

  lazy final override private[Routes] val getChildRoute: Seq[RouteTrait] = Seq(
    {
      type INPUT = (User.PK, String, TABLE_NAME, Commentable.PK)
      new RouteSchema[Comment.PK, Comment, INPUT] {
        lazy final override val encoder: Encoder[Comment] = implicitly
        lazy final override val tag:     ClassTag[Comment] = implicitly
        lazy final override val environment: Any with Database with Console with Clock with Random =
          env
        lazy final override val getTableRef: TableRef[Comment.PK, Comment] = Comment
        lazy final override val parent:      Option[RouteTraitWithChild] = Some(self)

        override def make(i: INPUT): ZCREATE[Comment.PK, Comment] =
          Comment.apply(i._1, i._2, i._3, i._4)

        lazy final override val httpCreateSchemaForm: Directive[INPUT] =
          formFields(
            'authorId.as[User.PK],
            'content.as[String],
            'commentableType.as[TABLE_NAME],
            'commentableId.as[Commentable.PK]
          )
      }
    }, {
      type INPUT = (String, String, User.PK, Iterable[Tag.PK])
      new RouteSchema[Idea.PK, Idea, INPUT] {
        lazy final override val encoder: Encoder[Idea] = implicitly
        lazy final override val tag:     ClassTag[Idea] = implicitly
        lazy final override val environment: Any with Database with Console with Clock with Random =
          env
        lazy final override val getTableRef: TableRef[Idea.PK, Idea] = Idea
        lazy final override val parent:      Option[RouteTraitWithChild] = Some(self)

        override def make(i: INPUT): ZCREATE[Idea.PK, Idea] =
          Idea.apply(
            title = i._1,
            description = i._2,
            authorId = i._3,
            tagsIds = i._4.toSet
          )

        lazy final override val httpCreateSchemaForm: Directive[INPUT] =
          formFields(
            'title.as[String],
            'description.as[String],
            'authorId.as[User.PK],
            'tagIds.as[Tag.PK].*
          )
      }
    },
    new RouteSchema[Skill.PK, Skill, (String, Boolean)] {
      lazy final override val encoder: Encoder[Skill] = implicitly
      lazy final override val tag:     ClassTag[Skill] = implicitly
      lazy final override val environment: Any with Database with Console with Clock with Random =
        env
      lazy final override val getTableRef: TableRef[Skill.PK, Skill] = Skill
      lazy final override val parent:      Option[RouteTraitWithChild] = Some(self)

      override def make(i: (String, Boolean)): ZCREATE[Skill.PK, Skill] = Skill.apply(i._1, i._2)

      lazy final override val httpCreateSchemaForm: Directive[(String, Boolean)] =
        formFields('name.as[String], 'isVisible.as[Boolean])
    },
    new RouteSchema[Tag.PK, Tag, (String, Boolean)] {
      lazy final override val encoder: Encoder[Tag] = implicitly
      lazy final override val tag:     ClassTag[Tag] = implicitly
      lazy final override val environment: Any with Database with Console with Clock with Random =
        env
      lazy final override val getTableRef: TableRef[Tag.PK, Tag] = Tag
      lazy final override val parent:      Option[RouteTraitWithChild] = Some(self)

      override def make(i: (String, Boolean)): ZCREATE[Tag.PK, Tag] = Tag.apply(i._1, i._2)

      lazy final override val httpCreateSchemaForm: Directive[(String, Boolean)] =
        formFields('name.as[String], 'isVisible.as[Boolean])
    }, {
      type INPUT = (String, String, OAuth, Iterable[Skill.PK])
      new RouteSchema[User.PK, User, INPUT] {
        lazy final override val encoder: Encoder[User] = implicitly
        lazy final override val tag:     ClassTag[User] = implicitly
        lazy final override val environment: Any with Database with Console with Clock with Random =
          env
        lazy final override val getTableRef: TableRef[User.PK, User] = User
        lazy final override val parent:      Option[RouteTraitWithChild] = Some(self)

        override def make(i: INPUT): ZCREATE[User.PK, User] =
          User.apply(
            name = i._1,
            email = i._2,
            oauthToken = i._3,
            skills = i._4.toSet
          )

        lazy final override val httpCreateSchemaForm: Directive[INPUT] =
          formFields(
            'name.as[String],
            'email.as[String],
            'oauth.as[String],
            'skillIds.as[Skill.PK].*
          )
      }
    }, {
      type INPUT = (User.PK, TABLE_NAME, Votable.PK, Int)
      new RouteSchema[UserVotes.PK, UserVotes, INPUT] {
        lazy final override val encoder: Encoder[UserVotes] = implicitly
        lazy final override val tag:     ClassTag[UserVotes] = implicitly
        lazy final override val environment: Any with Database with Console with Clock with Random =
          env
        lazy final override val getTableRef: TableRef[UserVotes.PK, UserVotes] = UserVotes
        lazy final override val parent:      Option[RouteTraitWithChild] = Some(self)

        override def make(i: INPUT): ZCREATE[UserVotes.PK, UserVotes] =
          UserVotes.apply(i._1, i._2, i._3, i._4)

        lazy final override val httpCreateSchemaForm: Directive[INPUT] =
          formFields(
            'userId.as[User.PK],
            'votableType.as[TABLE_NAME],
            'votableId.as[Votable.PK],
            'vote.as[Int]
          )
      }
    }
  )

  lazy final override val url: String = "api"

  override def methodGetOutput(): MessageSerializer = {
    ToMessage.RichMessage[Seq[RouteDescriptions]](
      operation = getFullUrl,
      status = MessageStatus.Success,
      fieldName = "routes"
    ) {
      getRoutes(getChildRoute)
    }
  }
}
