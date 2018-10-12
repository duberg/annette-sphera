package annette.core.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{ Directives, Route }
import akka.util.Timeout
import annette.core.json._
import annette.core.notification._
import annette.core.security.SecurityDirectives
import annette.core.utils.Generator

import scala.concurrent.ExecutionContext

trait VerificationRoutes extends Directives with Generator {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  val httpUrl: String
  val annetteSecurityDirectives: SecurityDirectives
  val notificationManager: NotificationManager

  def verify = (pathPrefix(JavaUUID) & pathPrefix(JavaUUID) & get & pathEndOrSingleSlash) { (x, y) =>
    onSuccess(notificationManager.verify(x, y).map(_.asJson)) & redirect(httpUrl, PermanentRedirect)
  }

  def verificationRoutes: Route = pathPrefix("verification") {
    verify
  }
}
