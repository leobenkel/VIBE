package com.leobenkel.vibe.client.routes.Framework

import com.leobenkel.vibe.client.app.AppState
import com.leobenkel.vibe.client.routes.Framework.RouteEngine.{BackendCore, StateCore}
import com.leobenkel.vibe.client.util.{ErrorProtection, Log}
import japgolly.scalajs.react.React.Context
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CtorType, _}

import scala.util._

private[routes] trait RouteEngine[S <: StateCore, B <: BackendCore[S]] {
  lazy final protected val selfRouteEngine: this.type = this
  lazy private val url:                     String = owner.url
  protected def owner:     RouteTrait
  protected def initState: S
  protected def initBackend(initState: BackendScope[Unit, S]): B

  lazy final private val component: Scala.Component[Unit, S, B, CtorType.Nullary] =
    ErrorProtection {
      ScalaComponent
        .builder[Unit](url)
        .initialState(initState)
        .backend[B](initBackend)
        .renderS { case (step3, state) => step3.backend.render(state) }
        .componentDidMount($ => $.backend.init($.state) >> $.backend.refresh($.state))
        .build
    }

  final def apply(): Scala.Unmounted[Unit, S, B] = ErrorProtection(component())
}

object RouteEngine {
  type Engine[S <: StateCore] = RouteEngine[S, BackendCore[S]]

  trait StateCore {
    def errors: Option[String] = None
  }

  trait BackendCore[S <: StateCore] {
    //scalastyle:off
    def $ : BackendScope[_, S]
    //scalastyle:on

    def owner: RouteTrait

    def setError(
      state:    S,
      newError: String
    ): S

    lazy final val appContext: Context[AppState] = owner.appContext

    def init(state:          S): Callback
    final def refresh(state: S): Callback =
      ErrorProtection {
        RouteTrait.GetHostUrl
          .flatMap {
            case Left(error) =>
              Log.error(s"Error: $error")
              AsyncCallback.apply[CallbackTo[Unit]](_ => $.modState(setError(_, newError = error)))
            case Right(host) =>
              refreshImpl(host, state)
          }
          .completeWith(_.get)
      }

    protected def refreshImpl(
      host:  String,
      state: S
    ): AsyncCallback[CallbackTo[Unit]]

    final def render(state: S): VdomElement =
      ErrorProtection(appContext.consume(renderImpl(_, state)))

    def renderImpl(
      appState: AppState,
      state:    S
    ): VdomElement
  }
}
