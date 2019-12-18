package com.leobenkel.vibe.server.Routes.Utils

private[Routes] object RouteUtils {

  case class RouteDescriptions(
    url:    String,
    depth:  Int,
    method: String,
    path:   Seq[RouteDescriptions]
  )

  def getRoutes(
    routes: Seq[RouteTrait],
    depth:  Int = 0
  ): Seq[RouteDescriptions] = {
    routes.map {
      case route: RouteTraitWithChild =>
        RouteDescriptions(
          url = route.getFullUrl,
          depth = depth,
          method = route.method.value,
          path = getRoutes(
            routes = route.getChildRoute.sortBy(rr => (rr.getFullUrl, rr.method.value)),
            depth = depth + 1
          )
        )
      case r: RouteTrait =>
        RouteDescriptions(
          url = r.getFullUrl,
          depth = depth,
          method = r.method.value,
          path = Seq.empty
        )
    }
  }
}
