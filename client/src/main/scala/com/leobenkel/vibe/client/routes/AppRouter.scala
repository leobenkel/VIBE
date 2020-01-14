package com.leobenkel.vibe.client.routes

import java.time.LocalDate

import com.leobenkel.vibe.client.components.AbstractComponent
import com.leobenkel.vibe.client.pages.ListPageForTable
import com.leobenkel.vibe.core.Schemas
import com.leobenkel.vibe.core.Schemas.Tag
import com.leobenkel.vibe.core.Schemas.Traits.TableRef
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import japgolly.scalajs.react.extra.router.StaticDsl.RouteB
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ReactMouseEventFrom}
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

  sealed trait AppPageData

  case object MainPageData extends AppPageData

  private def setEH(
    c:      RouterCtl[AppPageData],
    target: AppPageData
  ): (ReactMouseEventFrom[HTMLAnchorElement], MenuItemProps) => Callback = {
    (event: ReactMouseEventFrom[HTMLAnchorElement], data: MenuItemProps) =>
      c.setEH(target)(event)
  }

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
            MenuItem(
              active = resolution.page == MainPageData,
              onClick = { (event: ReactMouseEventFrom[HTMLAnchorElement], data: MenuItemProps) =>
                page.setEH(MainPageData)(event)
              }
            )("Main Page")
          )
        ),
        <.div(^.flex := "1 1  auto", resolution.render())
      )
    )
  }

  private val Config: RouterConfig[AppPageData] = RouterConfigDsl[AppPageData].buildConfig { dsl =>
    import dsl._

    val seqInt = new RouteB[Seq[Int]](
      regex = "(-?[\\d,]+)",
      matchGroups = 1,
      parse = { groups =>
        Some(groups(0).split(",").map(_.toInt))
      },
      build = _.mkString(",")
    )

    val dateRange = new RouteB[(LocalDate, LocalDate)](
      regex = "\\((.*),(.*)\\)",
      matchGroups = 2,
      parse = { groups =>
        Some((LocalDate.parse(groups(0)), LocalDate.parse(groups(1))))
      },
      build = { tuple =>
        s"(${tuple._1.toString},${tuple._2.toString})"
      }
    )

    (trimSlashes
      | staticRoute("mainPage", MainPageData) ~> renderR(
        (_: RouterCtl[AppPageData]) =>
          new ListPageForTable[Tag.PK, Tag]() {
            lazy final override protected val getTableRef: TableRef[Schemas.Tag.PK, Tag] = Tag
//            implicit lazy final override protected val reader:  RW[ReturnType] = macroRW
//            implicit lazy final override protected val readerC: RW[ContentS[Tag]] = macroRW
//            implicit lazy final override protected val readerL: RW[Array[Tag]] = implicitly
//            implicit lazy final override protected val readerT: RW[Tag] = macroRW

            implicit lazy final override val decoderT: Decoder[DecodingType] =
              new Decoder[DecodingType] {

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
//            implicit lazy final override val classTagT: ClassTag[Tag] = implicitly
          }.apply()
      ))
      .notFound(redirectToPage(MainPageData)(Redirect.Replace))
      .renderWith(layout)
  }
  private val UrlBase: BaseUrl = BaseUrl.fromWindowOrigin_/

  val Routes: Router[AppPageData] = Router.apply(UrlBase, Config)
}
