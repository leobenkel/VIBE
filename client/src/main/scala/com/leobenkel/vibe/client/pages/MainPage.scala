package com.leobenkel.vibe.client.pages

import com.leobenkel.vibe.client.components.AbstractComponent
import com.leobenkel.vibe.client.schemas.Temp
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLButtonElement
import typingsJapgolly.semanticDashUiDashReact.components._
import typingsJapgolly.semanticDashUiDashReact.distCommonjsElementsButtonButtonMod.ButtonProps
import ujson.Value.InvalidData
import upickle.default._

/**
  * A "page" in the application, in this same directory you'd put all of the other application "pages".
  * These are not html pages per se, since we're dealing with a single page com.leobenkel.vibe.client.app. But it's useful to treat
  * each of these as pages internally.
  */
object MainPage extends AbstractComponent {
  type T = Temp
  case class State(objects: Seq[T] = Seq.empty)

  class Backend($ : BackendScope[_, State]) {
    def init(state:    State): Callback = Callback.empty
    def refresh(state: State): Callback =
      Ajax
        // TODO move the root of the com.leobenkel.vibe.client.app to a config file
        .get("http://localhost:8079/api/")
        .send
        .asAsyncCallback
        .map { xhr =>
          import com.leobenkel.vibe.client.util.ModelPickler._
          try {
            val objects = read[Seq[T]](xhr.responseText)
            $.modState(_.copy(objects = objects))
          } catch {
            case e: InvalidData =>
              dom.console.error(e.msg + ":" + e.data)
              throw e
          }
        }
        .completeWith(_.get)

    def onAddNewObject(
      event: ReactMouseEventFrom[HTMLButtonElement],
      data:  ButtonProps
    ): Callback =
      Callback.alert(
        "Clicked on 'Add New object'... did you expect something else? hey, I can't write everything for you!"
      )

    def render(state: State): VdomElement =
      appContext.consume { appState =>
        <.div(
          Table()(
            TableHeader()(
              TableRow()(
                TableHeaderCell()("Id"),
                TableHeaderCell()("Name")
              )
            ),
            TableBody()(
              state.objects.toVdomArray { obj =>
                TableRow()(
                  TableCell()(obj.id),
                  TableCell()(obj.name)
                )
              }
            )
          ),
          Button(onClick = onAddNewObject _)("Add new object")
        )
      }
  }
  private val component = ScalaComponent
    .builder[Unit]("MainPage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.init($.state) >> $.backend.refresh($.state))
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()
}
