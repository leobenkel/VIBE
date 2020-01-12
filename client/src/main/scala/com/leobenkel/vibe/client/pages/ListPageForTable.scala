package com.leobenkel.vibe.client.pages

import com.leobenkel.vibe.client.app.Config
import com.leobenkel.vibe.client.components.AbstractComponent
import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.raw.React
import japgolly.scalajs.react.raw.React.Ref
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CtorType, _}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLButtonElement
import typingsJapgolly.semanticDashUiDashReact.components._
import typingsJapgolly.semanticDashUiDashReact.distCommonjsElementsButtonButtonMod.ButtonProps
import ujson.Value.InvalidData
import upickle.default._

import scala.scalajs.js.|

/**
  * A "page" in the application, in this same directory you'd put all of the other application "pages".
  * These are not html pages per se, since we're dealing with a single page com.leobenkel.vibe.client.app. But it's useful to treat
  * each of these as pages internally.
  */
trait ListPageForTable[T] extends AbstractComponent {
  private case class State(
    objects: Seq[T] = Seq.empty,
    errors:  Option[String] = None
  )

  protected def getHeaderColumns: Seq[Symbol]
  protected def getTableValues(obj: T): Seq[ChildArg]

  protected def reader: upickle.default.Reader[Seq[T]]

  lazy private val getHostUrl: AsyncCallback[Either[String, String]] = for {
    host <- Config.getKey("host")
    port <- Config.getKey("port")
  } yield {
    (for {
      p <- port.flatMap {
        _.asNumber
          .flatMap(_.toInt)
          .fold[Either[String, Int]](Left("Could not covert to 'Int'"))(Right(_))
      }
      h <- host
        .flatMap {
          _.asString
            .fold[Either[String, String]](Left("Could not covert to 'String'"))(Right(_))
        }
    } yield {
      s"$h:$p"
    })
  }

  class Backend($ : BackendScope[_, State]) {
    def init(state:    State): Callback = Callback.empty
    def refresh(state: State): Callback = {
      getHostUrl
        .flatMap {
          case Left(error) =>
            println(s"Error: $error")
            AsyncCallback.apply[CallbackTo[Unit]](_ => $.modState(_.copy(errors = Some(error))))
          case Right(host) =>
            Ajax
              .get(s"http://$host/api/tags/all")
              .setRequestContentTypeJsonUtf8
              .send
              .asAsyncCallback
              .map { xhr =>
                try {
                  println(s"${xhr.responseText}")
                  val objects: Seq[T] = read[Seq[T]](xhr.responseText)(reader)
                  $.modState(_.copy(objects = objects))
                } catch {
                  case e: InvalidData =>
                    dom.console.error(e.msg + ":" + e.data)
                    throw e
                }
              }
        }.completeWith(_.get)
    }

    def onAddNewObject(
      event: ReactMouseEventFrom[HTMLButtonElement],
      data:  ButtonProps
    ): Callback =
      Callback.alert(
        "Clicked on 'Add New object'... did you expect something else? hey, " +
          "I can't write everything for you!"
      )

    def render(state: State): VdomElement =
      appContext.consume { _ =>
        <.div(
          Table()(
            TableHeader()(
              TableRow()(
                getHeaderColumns
                  .map(n => TableHeaderCell()(VdomNode.cast(n.name)))
                  .map(VdomNode.cast): _*
              )
            ),
            TableBody()(
              state.objects.toVdomArray { obj =>
                TableRow()(
                  getTableValues(obj)
                    .map(r => TableCell()(r))
                    .map(VdomNode.cast): _*
                )
              }
            )
          ),
          Button(onClick = onAddNewObject)("Add new object")
        )
      }
  }

  private val component: Component[Unit, State, Backend, CtorType.Nullary] = ScalaComponent
    .builder[Unit]("MainPage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.init($.state) >> $.backend.refresh($.state))
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

  override def rawElement: React.Element = new React.Element {
    override def key: Key | Null = ???

    override def ref: Ref | Null = ???
  }
}
