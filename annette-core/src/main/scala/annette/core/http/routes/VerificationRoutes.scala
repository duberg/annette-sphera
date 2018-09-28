package annette.core.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{ Directives, Route }
import akka.util.Timeout
import annette.core.json.{ JsonNotification, _ }
import annette.core.notification._
import annette.core.security.AnnetteSecurityDirectives
import annette.core.security.verification.Verification
import annette.core.utils.Generator

import scala.concurrent.ExecutionContext
import scala.util._

trait VerificationRoutes extends Directives with Generator {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  val httpUrl: String
  val annetteSecurityDirectives: AnnetteSecurityDirectives
  val notificationManager: NotificationManager

  import annetteSecurityDirectives._

  def verify = (pathPrefix(JavaUUID) & pathPrefix(JavaUUID) & get & pathEndOrSingleSlash) { (x, y) =>
    onSuccess(notificationManager.verify(x, y).map(_.asJson)) & redirect(httpUrl, PermanentRedirect)
  }

  def verificationRoutes: Route = pathPrefix("verification") {
    verify
  }
}
