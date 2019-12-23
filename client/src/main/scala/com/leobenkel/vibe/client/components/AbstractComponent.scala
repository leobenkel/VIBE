package com.leobenkel.vibe.client.components

import com.leobenkel.vibe.client.app.AppState
import japgolly.scalajs.react.React.Context

/**
 * An abstract component trait from which all components in the com.leobenkel.vibe.client.app should derive. A good
 * place to put in global implicits, common code that should be in all pages, etc.
 */
trait AbstractComponent {
  val appContext: Context[AppState] = AppState.CTX
}
