package com.leobenkel.vibe.client.css

import scalacss.ProdDefaults._
import scalacss.internal.mutable.GlobalRegistry

object AppCSS {
  object Style extends StyleSheet.Inline {

    import dsl._

    val navMenu: StyleA = style(
      display.flex,
      alignItems.center,
      backgroundColor(c"#F2706D"),
      margin.`0`,
      listStyle := "none"
    )

    val menuItem: Boolean => StyleA = styleF.bool(
      selected =>
        styleS(
          padding(20.px),
          fontSize(1.5.em),
          cursor.pointer,
          color(c"rgb(244, 233, 233)"),
          whiteSpace.nowrap,
          mixinIfElse(selected)(backgroundColor(c"#E8433F"), fontWeight._500)(
            &.hover(backgroundColor(c"#B6413E"))
          )
        )
    )
  }

  def load(): Unit = {
    GlobalRegistry.register(
      GlobalStyle,
      Style
    )
    GlobalRegistry.onRegistration(_.addToDocument())
  }
}
