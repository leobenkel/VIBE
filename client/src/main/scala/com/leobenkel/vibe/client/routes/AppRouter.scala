package com.leobenkel.vibe.client.routes

import com.leobenkel.vibe.client.components.AbstractComponent
import com.leobenkel.vibe.client.routes.Framework.RouteTrait
import com.leobenkel.vibe.client.routes.pages.ListPageForTable
import com.leobenkel.vibe.client.routes.pages.ListPageForTable.SchemaAllPage
import com.leobenkel.vibe.client.util.{ErrorProtection, Log}
import com.leobenkel.vibe.core.Schemas.Traits.TableRef
import com.leobenkel.vibe.core.Schemas._
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import japgolly.scalajs.react.ReactMouseEventFrom
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLAnchorElement
import typingsJapgolly.semanticDashUiDashReact.components._
import typingsJapgolly.semanticDashUiDashReact.distCommonjsCollectionsMenuMenuItemMod._

/**
  * The com.leobenkel.vibe.client.app's router. It has two main responsibilities:
  * - Present an com.leobenkel.vibe.client.app menu
  * - Choose which "page" to present depending on the route (i.e. the url)
  */
object AppRouter extends AbstractComponent {

  case class State()

  trait AppPageData

//  private def setEH(
//    c:      RouterCtl[AppPageData],
//    target: AppPageData
//  ): (ReactMouseEventFrom[HTMLAnchorElement], MenuItemProps) => Callback = {
//    (event: ReactMouseEventFrom[HTMLAnchorElement], data: MenuItemProps) =>
//      c.setEH(target)(event)
//  }

  private def layout(
    page:       RouterCtl[AppPageData],
    resolution: Resolution[AppPageData]
  ): VdomTagOf[Div] = {
    assert(page != null)
    <.div(
      ^.height := 100.pct,
      <.div(
        ^.height        := 100.pct,
        ^.className     := "full height",
        ^.display       := "flex",
        ^.flexDirection := "row",
        <.div(
          ^.height    := 100.pct,
          ^.className := "no-print",
          ^.flex      := "0 0  auto",
          ^.position  := "relative",
          Menu(vertical = true)(
            AllSchemas.toVdomArray { schema =>
              MenuItem(
                active = resolution.page == schema.PageData,
                onClick = { (event: ReactMouseEventFrom[HTMLAnchorElement], _: MenuItemProps) =>
                  page.setEH(schema.PageData)(event)
                }
              )(schema.name)
            }
          )
        ),
        <.div(^.flex := "1 1  auto", resolution.render())
      )
    )
  }

  private val AllSchemas: Seq[RouteTrait] = Seq(
    new ListPageForTable[Tag.PK, Tag]() {
      lazy final override protected val getTableRef: TableRef[Tag.PK, Tag] = Tag

      implicit lazy final override val decoderT: Decoder[DecodingType] = new Decoder[DecodingType] {
        override def apply(c: HCursor): Result[DecodingType] = {
          for {
            id                <- c.downField("id").as[Tag.PK]
            creationTimestamp <- c.downField("creationTimestamp").as[Long]
            updateTimestamp   <- c.downField("updateTimestamp").as[Long]
            name              <- c.downField("name").as[String]
            isVisible         <- c.downField("isVisible").as[Boolean]
          } yield {
            Tag(
              id = id,
              creationTimestamp = creationTimestamp,
              updateTimestamp = updateTimestamp,
              name = name,
              isVisible = isVisible
            )
          }
        }
      }
    }.asInstanceOf[SchemaAllPage[Tag.PK]],
    new ListPageForTable[Skill.PK, Skill]() {
      lazy final override protected val getTableRef: TableRef[Skill.PK, Skill] = Skill

      implicit lazy final override val decoderT: Decoder[DecodingType] = new Decoder[DecodingType] {
        override def apply(c: HCursor): Result[DecodingType] = {
          for {
            id                <- c.downField("id").as[Skill.PK]
            creationTimestamp <- c.downField("creationTimestamp").as[Long]
            updateTimestamp   <- c.downField("updateTimestamp").as[Long]
            name              <- c.downField("name").as[String]
            isVisible         <- c.downField("isVisible").as[Boolean]
          } yield {
            Skill(
              id = id,
              creationTimestamp = creationTimestamp,
              updateTimestamp = updateTimestamp,
              name = name,
              isVisible = isVisible
            )
          }
        }
      }
    }.asInstanceOf[SchemaAllPage[Skill.PK]],
    new ListPageForTable[User.PK, User]() {
      lazy final override protected val getTableRef: TableRef[User.PK, User] = User

      implicit lazy final override val decoderT: Decoder[DecodingType] = new Decoder[DecodingType] {
        override def apply(c: HCursor): Result[DecodingType] = {
          for {
            id                <- c.downField("id").as[User.PK]
            creationTimestamp <- c.downField("creationTimestamp").as[Long]
            updateTimestamp   <- c.downField("updateTimestamp").as[Long]
            name              <- c.downField("name").as[String]
            email             <- c.downField("email").as[String]
            oauthToken        <- c.downField("oauthToken").as[User.OAuth]
            skills            <- c.downField("skills").as[List[Skill.PK]]
          } yield {
            User(
              id = id,
              creationTimestamp = creationTimestamp,
              updateTimestamp = updateTimestamp,
              name = name,
              email = email,
              oauthToken = oauthToken,
              skills = skills.toSet
            )
          }
        }
      }
    }.asInstanceOf[SchemaAllPage[User.PK]]
  )

  private val Config: RouterConfig[AppPageData] = RouterConfigDsl[AppPageData].buildConfig { dsl =>
    import dsl._

//    val seqInt = new RouteB[Seq[Int]](
//      regex = "(-?[\\d,]+)",
//      matchGroups = 1,
//      parse = { groups =>
//        Some(groups(0).split(",").map(_.toInt))
//      },
//      build = _.mkString(",")
//    )
//
//    val dateRange = new RouteB[(LocalDate, LocalDate)](
//      regex = "\\((.*),(.*)\\)",
//      matchGroups = 2,
//      parse = { groups =>
//        Some((LocalDate.parse(groups(0)), LocalDate.parse(groups(1))))
//      },
//      build = { tuple =>
//        s"(${tuple._1.toString},${tuple._2.toString})"
//      }
//    )

    ErrorProtection {
      AllSchemas
        .foldLeft[Rule](trimSlashes) {
          case (acc: Rule, schema) =>
            Log.info(s"Set up route for ${schema.name}")
            acc | schema.route(dsl)
        }
        .notFound(redirectToPage {
          // TODO: replace with 404 page here
          AllSchemas.head.PageData
        }(Redirect.Replace))
        .renderWith(layout)
    }
  }

  private val UrlBase: BaseUrl = BaseUrl.fromWindowOrigin_/

  val Routes: Router[AppPageData] = Router.apply(UrlBase, Config)
}
