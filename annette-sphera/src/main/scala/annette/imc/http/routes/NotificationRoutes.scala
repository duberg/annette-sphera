package annette.imc.http.routes

import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives.{ entity, pathPrefix, _ }
import akka.http.scaladsl.server.{ Directive1, Route, StandardRoute }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import akka.http.scaladsl.model.headers._
import annette.core.AnnetteException
import annette.imc.notification._

import scala.util.{ Failure, Success }

trait NotificationRoutes
  extends NotificationConfig { self: APIContext with API =>

  def notificationRoutes: Route = {
    pathPrefix("notification") {
      pathPrefix("sms") {
        // imc/api/notification/sms/add-status-verification
        (path("add-status-verification") & get & auth) { sessionData =>
          val userId = sessionData.userId
          val f = for {
            user <- coreModule.userManager.getById(userId).map(_.get) if user.phone.nonEmpty
            x <- notificationService.addSmsVerificationStatus(user.phone.get, user.language.getOrElse(""))
          } yield x
          onComplete(f) {
            case Success(x) => complete(x.id)
            case Failure(throwable) =>
              throwable match {
                case annetteException: AnnetteException =>
                  complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
                case _ =>
                  complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
              }
          }
        } ~
          (path("verify" / JavaUUID) & post & auth & entity(as[String])) { (id, sessionData, code) =>
            onComplete(notificationService.smsVerify(id, code)) {
              case Success(x) => complete(x)
              case Failure(throwable) =>
                throwable match {
                  case annetteException: AnnetteException =>
                    complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
                  case _ =>
                    complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
                }
            }
          }
      }
    }
  }
}
