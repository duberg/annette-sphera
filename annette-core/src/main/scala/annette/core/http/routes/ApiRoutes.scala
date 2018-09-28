package annette.core.http.routes

import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.AskSupport
import com.typesafe.config.Config

trait ApiRoutes extends Directives with AskSupport
  with AuthRoutes
  with UserRoutes
  with NotificationRoutes
  with VerificationRoutes {

  val config: Config
  val httpConfig = config.getConfig("annette.http")
  val httpHost = httpConfig.getString("host")
  val httpPort = httpConfig.getString("port")
  val httpUrl = s"http://$httpHost:$httpPort"
  val apiUrl = s"$httpUrl/api"

  val apiRoutes: Route = pathPrefix("api") {
    authRoutes ~ userRoutes ~ notificationRoutes ~ verificationRoutes
  }
}
