package annette.imc.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives.{ entity, pathPrefix, _ }
import akka.http.scaladsl.server.{ Directive1, Route }
import akka.pattern.ask
import akka.util.Timeout
import akka.stream.ActorMaterializer
import annette.imc.ApsActor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import akka.http.scaladsl.model.headers._
import scala.concurrent.duration._
import annette.core.exception.AnnetteException
import annette.core.services.authentication.SessionData
import annette.imc.model.{ ApStatus, _ }
import annette.imc.notification._
import scala.util.{ Failure, Success }
import annette.imc.utils.Implicits._
import annette.core.utils.Generator

trait BulletinRoutes
  extends NotificationConfig
  with Generator { self: APIContext with API =>

  private val update = (path("update" / JavaUUID) & post & auth & entity(as[UpdateBulletin])) {
    (apId, sessionData, bulletin) =>
      {
        val userId = sessionData.userId

        if (bulletin.expertId == userId) {

          val x: Boolean = bulletin.isFinished match {
            case Some(true) => true
            case _ => false
          }

          if (x) {
            // ========= Verification =========

            val f = for {
              user <- coreModule.userDao.getById(userId).map(_.get) if user.phone.nonEmpty
              x <- notificationService.addSmsVerificationVoted(user.phone.get, apId, bulletin, user.defaultLanguage)
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
          } else {

            val f = apsActor.ask(UpdateBulletinCmd(apId, bulletin))
            onComplete(f) {
              case Success(Done) => complete("Done")
              case Success(NotFound) => complete("NotFound")
              case Success(ApNotExists) => complete("ApNotExists")
              case Success(_) => complete(StatusCodes.InternalServerError)
              case Failure(throwable) =>
                throwable match {
                  case annetteException: AnnetteException =>
                    complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
                  case _ =>
                    complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
                }
            }

          }

        } else {
          complete("No such expert")
        }
      }
  }

  private val showcase = (path("showcase" / JavaUUID) & get & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {
          case Success(ApFound(x)) =>
            val expertise = x.expertise
            val showcase = ShowcaseExpertise(
              expertise.experts,
              expertise.bulletins.values.toList)
            complete(showcase.asJson)
          case Success(ApNotExists) => complete("ApNotExists")
          case Success(_) => complete(StatusCodes.InternalServerError)
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

  private val getBulletin = (path("get" / JavaUUID) & get & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {
          case Success(ApFound(x)) =>
            val expertise = x.expertise
            expertise.bulletins.get(userId) match {
              case Some(x) => complete(x.asJson)
              case None => complete(NotFound)
            }

          case Success(ApNotExists) => complete("ApNotExists")
          case Success(_) => complete(StatusCodes.InternalServerError)
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

  def bulletinRoutes: Route = pathPrefix("bulletin") {
    update ~ showcase ~ getBulletin
  }
}
