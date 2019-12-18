package com.leobenkel.vibe.server.Routes

import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.server.{Route, _}
import com.leobenkel.vibe.core.Schemas.Traits._
import com.leobenkel.vibe.core.Schemas.User.OAuth
import com.leobenkel.vibe.core.Schemas._
import com.leobenkel.vibe.core.Services.Database
import com.leobenkel.vibe.core.Utils.SchemaTypes.TABLE_NAME
import com.leobenkel.vibe.server.Environment.LiveEnvironment
import com.leobenkel.vibe.server.Routes.Root.RootRoute
import com.leobenkel.vibe.server.Routes.Utils.RoutePutSchema.ZCREATE
import com.leobenkel.vibe.server.Routes.Utils._
import com.leobenkel.vibe.server.Schemas.ModelPickler
import com.leobenkel.vibe.server.Utils.ZIODirectives
import de.heikoseeberger.akkahttpupickle.UpickleSupport
import io.circe.Encoder
import io.circe.generic.auto._
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

import scala.reflect.ClassTag

trait FullRoutes
    extends RouteTraitWithChild with Directives with LiveEnvironment with UpickleSupport
    with ModelPickler with ZIODirectives
//    with ModelService
//    //TODO If you split your full route into different services, add them here
//    with HTMLService
    {
//  private val runtime: DefaultRuntime = new DefaultRuntime() {}
  protected def env: Any with Database with Console with Clock with Random

  override private[Routes] val getChildRoute: Seq[RouteTrait] = Seq(
    RootRoute(this), {
      type INPUT = (User.PK, String, TABLE_NAME, Commentable.PK)
      new RouteSchema[Comment.PK, Comment, INPUT] {
        lazy final override val encoder: Encoder[Comment] = implicitly
        lazy final override val tag:     ClassTag[Comment] = implicitly
        lazy final override val environment: Any with Database with Console with Clock with Random =
          env
        lazy final override val getTableRef: TableRef[Comment.PK, Comment] = Comment

        override def make(i: INPUT): ZCREATE[Comment] = Comment.apply(i._1, i._2, i._3, i._4)

        override def httpCreateSchemaForm(): Directive[INPUT] =
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

        override def make(i: INPUT): ZCREATE[Idea] =
          Idea.apply(
            title = i._1,
            description = i._2,
            authorId = i._3,
            tagsIds = i._4.toSet
          )

        override def httpCreateSchemaForm(): Directive[INPUT] =
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

      override def make(i: (String, Boolean)): ZCREATE[Skill] =
        Skill.apply(i._1, i._2)

      override def httpCreateSchemaForm(): Directive[(String, Boolean)] =
        formFields('name.as[String], 'isVisible.as[Boolean])
    },
    new RouteSchema[Tag.PK, Tag, (String, Boolean)] {
      lazy final override val encoder: Encoder[Tag] = implicitly
      lazy final override val tag:     ClassTag[Tag] = implicitly
      lazy final override val environment: Any with Database with Console with Clock with Random =
        env
      lazy final override val getTableRef: TableRef[Tag.PK, Tag] = Tag

      override def make(i: (String, Boolean)): ZCREATE[Tag] =
        Tag.apply(i._1, i._2)

      override def httpCreateSchemaForm(): Directive[(String, Boolean)] =
        formFields('name.as[String], 'isVisible.as[Boolean])
    }, {
      type INPUT = (String, String, OAuth, Iterable[Skill.PK])
      new RouteSchema[User.PK, User, INPUT] {
        lazy final override val encoder: Encoder[User] = implicitly
        lazy final override val tag:     ClassTag[User] = implicitly
        lazy final override val environment: Any with Database with Console with Clock with Random =
          env
        lazy final override val getTableRef: TableRef[User.PK, User] = User

        override def make(i: INPUT): ZCREATE[User] =
          User.apply(
            name = i._1,
            email = i._2,
            oauthToken = i._3,
            skills = i._4.toSet
          )

        override def httpCreateSchemaForm(): Directive[INPUT] =
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

        override def make(i: INPUT): ZCREATE[UserVotes] =
          UserVotes.apply(i._1, i._2, i._3, i._4)

        override def httpCreateSchemaForm(): Directive[INPUT] =
          formFields(
            'userId.as[User.PK],
            'votableType.as[TABLE_NAME],
            'votableId.as[Votable.PK],
            'vote.as[Int]
          )
      }
    }
  )

  lazy override val url: String = ""

  override val route: Route = DebuggingDirectives.logRequest("Request") {
    ignoreTrailingSlash(getChildRoute.map(_.route).reduce(_ ~ _))
  }
}
