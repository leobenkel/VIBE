/*
 * Copyright 2019 Roberto Leibman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leobenkel.vibe.client.app

import com.leobenkel.vibe.client.components.AbstractComponent
import com.leobenkel.vibe.client.routes.AppRouter
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CtorType, _}

/**
  * This is a helper class meant to load initial com.leobenkel.vibe.client.app state, scalajs-react normally
  * suggests (and rightfully so) that the router should be the main content of the com.leobenkel.vibe.client.app,
  * but having a middle piece that loads com.leobenkel.vibe.client.app state makes some sense, that way the router is in charge of routing and
  * presenting the com.leobenkel.vibe.client.app menu.
  */
object Content extends AbstractComponent {
  case class State(appState: AppState = AppState())

  class Backend($ : BackendScope[_, State]) {
    def render(s: State): VdomElement =
      appContext.provide(s.appState) {
        AppRouter.Routes()
      }

    def refresh(s: State): Callback =
      Callback.empty //TODO: add ajax calls to initialize com.leobenkel.vibe.client.app state here
  }

  private val Component: Component[Unit, State, Backend, CtorType.Nullary] = ScalaComponent
    .builder[Unit]("content")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.refresh($.state))
    .build

  def apply(): Unmounted[Unit, State, Backend] = Component()
}
