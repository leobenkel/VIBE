package com.leobenkel.vibe.core.Schemas.Traits

import com.leobenkel.vibe.core.Schemas._
import com.leobenkel.vibe.core.TestUtils.TestRuntime
import org.scalatest.{Assertion, FreeSpec}
import zio.ZIO

class VotableTest extends FreeSpec {
  "VotableTest" - {
    val runtime = TestRuntime()
    "Make User vote on Idea" - {
      val user = for {
        designer  <- Skill("design")
        userSkill <- Skill("user")
        user <- User(
          name = "userName",
          email = "email",
          oauthToken = "abcToken",
          skills = Set(designer, userSkill)
        )
      } yield {
        println(s"Created user with ID: ${user.id}")
        user
      }

      val idea = for {
        t1 <- Tag("tag1")
        t2 <- Tag("tag2")
        u  <- user
        idea <- Idea(
          title = "good idea",
          description = "this is awesome",
          author = u,
          tagsIds = Set(t1, t2)
        )
      } yield {
        println(s"Created idea with ID: ${idea.id}")
        idea
      }

      "Voting" in {
        val process: ZIO[TestRuntime.ENV, Throwable, Assertion] = for {
          u             <- user
          i             <- idea
          newIdea       <- i.voteUpBy(u)
          originalScore <- i.score
          newScore      <- newIdea.score
        } yield {
          assert(originalScore == 0)
          assert(newScore == 1)
        }

        val output = runtime.unsafeRunSync(process).mapError(throw _)
      }
    }
  }
}
