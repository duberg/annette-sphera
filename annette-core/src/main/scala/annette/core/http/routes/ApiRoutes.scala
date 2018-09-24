package annette.core.http.routes

import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.AskSupport

trait ApiRoutes extends Directives with AskSupport with AuthRoutes with UsersRoutes {
  val apiRoutes: Route = pathPrefix("api" / "v1") {
    authRoutes ~ userRoutes
  }
}
