package com.leobenkel.vibe.server.Routes.Utils

private[Routes] object RouteUtils {

  case class RouteDescriptions(
    url:   String,
    depth: Int,
    path:  Seq[RouteDescriptions]
  )

  def getRoutes(
    routes: Seq[RouteTrait],
    depth:  Int = 0
  ): Seq[RouteDescriptions] = {
    routes.map {
      case r: RouteTraitWithChild =>
        RouteDescriptions(
          url = r.getFullUrl,
          depth = depth,
          path = getRoutes(routes = r.getChildRoute, depth = depth + 1)
        )
      case r: RouteTrait =>
        RouteDescriptions(
          url = r.getFullUrl,
          depth = depth,
          path = Seq.empty
        )
    }
  }
}
