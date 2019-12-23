package com.leobenkel.vibe.client.css

import scalacss.ProdDefaults._

object GlobalStyle extends StyleSheet.Inline {

  import dsl._

  style(
    unsafeRoot("body")(
      margin.`0`,
      padding(2.px),
      fontSize(14.px),
      font := "normal small 'Roboto', sans-serif"
    ),
    unsafeRoot("h2")(marginTop(1.em))
  )
}
