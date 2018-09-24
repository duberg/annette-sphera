package annette.imc.http.routes

import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives.{ pathPrefix, _ }
import akka.pattern.ask
import annette.imc.ApsActor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import annette.core.exception.AnnetteException
import annette.core.services.authentication.Session
import annette.imc.user.model.UserRoled

import scala.util.{ Failure, Success }
import annette.imc.utils.Implicits._

trait ExpertRoutes { self: APIContext with API =>

  private val add = (path("add" / JavaUUID / JavaUUID) & get & auth) {
    (apId, expertId, sessionData: Session) =>
      {
        val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]

        val ff = for {

          u <- userFuture
          _ <- predicate(u.exists(_.admin))(new Exception("must be in role of admin"))

          f <- apsActor.ask(AddExpertCmd(apId, expertId))
        } yield f

        onComplete(ff) {
          case Success(Done) => complete("Done")
          case Success(EnterEntityNameFirst) => complete("EnterEntityNameFirst")
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

  private val remove = (path("remove" / JavaUUID / JavaUUID) & get & auth) {
    (apId, expertId, sessionData: Session) =>
      {
        val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]

        val ff = for {

          u <- userFuture
          _ <- predicate(u.exists(_.admin))(new Exception("must be in role of admin"))

          f <- apsActor.ask(RemoveExpertCmd(apId, expertId))
        } yield f

        onComplete(ff) {
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
  }

  private val all = (path("all" / JavaUUID) & get & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {
          case Success(ApFound(x)) =>
            complete(x.expertise.experts)
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

  def expertRoutes = pathPrefix("expert") {
    add ~ remove ~ all
  }

}
