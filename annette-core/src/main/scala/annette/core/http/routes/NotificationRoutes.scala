package annette.core.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{ Directives, Route }
import akka.util.Timeout
import annette.core.json.{ JsonNotification, _ }
import annette.core.notification._
import annette.core.security.SecurityDirectives
import annette.core.security.verification.Verification
import annette.core.utils.Generator

import scala.concurrent.ExecutionContext

trait NotificationRoutes extends Directives with Generator {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  val annetteSecurityDirectives: SecurityDirectives
  val notificationService: NotificationService

  import annetteSecurityDirectives._

  def push = (pathPrefix("notifications") & post & entity(as[JsonNotification]) & pathEndOrSingleSlash) { x =>
    complete {
      val (Some(field), index) = Seq(x.email, x.phone)
        .zipWithIndex
        .filter(_._1.nonEmpty)
        .head

      val notification = index match {
        case 0 => CreateEmailNotification(
          email = field,
          subject = x.subject,
          message = x.message)
        case 1 => CreateSmsNotification(
          phone = field,
          subject = x.subject,
          message = x.message)
      }

      notificationService.push(notification)

      Accepted
    }
  }

  /**
   * При сбросе сообщения [[Verification]] автоматически создается верификация.
   */

  //  def pushVer = (pathPrefix("verifications") & post & entity(as[JsonVerification]) & pathEndOrSingleSlash) { x =>
  //    complete {
  //      val notification = SmsVerification(
  //        id = generateUUID,
  //        phone = x.phone,
  //        subject = x.subject,
  //        message = x.message,
  //        code = x.code)
  //
  //      notificationService.push(notification)
  //
  //      Accepted -> Response(
  //        entityId = notification.id.toString,
  //        status = ResponseStatus.Pending)
  //    }
  //  }

  //  def verify = (pathPrefix("verifications" / JavaUUID) & entity(as[String]) & post & pathEndOrSingleSlash) { (x, y) =>
  //    complete(notificationService.verify(x, y).map(_.asJson))
  //  }

  // def sms = ???
  //def email = pathPrefix("email")

  def notificationRoutes: Route = (pathPrefix("push") & authenticated) { implicit session =>
    push
  }
}
