package com.leobenkel.vibe.client.pages

import com.leobenkel.vibe.client.app.Config
import com.leobenkel.vibe.client.components.AbstractComponent
import com.leobenkel.vibe.core.Schemas.Tag
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
  type T = Tag
  case class State(
    objects: Seq[T] = Seq.empty,
    errors:  Option[String] = None
  )

  class Backend($ : BackendScope[_, State]) {
    def init(state:    State): Callback = Callback.empty
    def refresh(state: State): Callback = {
      Config
        .getKey("host")
        .flatMap { host =>
          Config
            .getKey("port")
            .flatMap { port =>
              println(s"h: $host")
              println(s"p: $port")

              val out: AsyncCallback[CallbackTo[Unit]] = (for {
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
                Ajax
                  .get(s"http://$h:$p/api/tags/all")
                  .setRequestContentTypeJsonUtf8
                  .send
                  .asAsyncCallback
                  .map { xhr =>
                    import com.leobenkel.vibe.client.util.ModelPickler._
                    try {
                      println(s"${xhr.responseText}")
                      val objects = read[Seq[T]](xhr.responseText)
                      $.modState(_.copy(objects = objects))
                    } catch {
                      case e: InvalidData =>
                        dom.console.error(e.msg + ":" + e.data)
                        throw e
                    }
                  }
              }) match {
                case Left(error) =>
                  println(s"Error: $error")
                  AsyncCallback.apply { _ =>
                    $.modState(_.copy(errors = Some(error)))
                  }
                case Right(r) => r
              }

              out
            }
        }
    }.completeWith(_.get)

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
