package annette.core.http.routes

import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.AskSupport

trait ApiRoutes extends Directives with AskSupport
  with AuthRoutes
  with UserRoutes
  with NotificationRoutes {

  val apiRoutes: Route = pathPrefix("api") {
    extractUri { uri =>
      println(uri)
      authRoutes ~ userRoutes ~ notificationRoutes
    }
  }
}
