package annette.imc.http.routes

import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives.{ entity, pathPrefix, _ }
import akka.http.scaladsl.server.Directive1
import akka.pattern.ask
import akka.util.Timeout
import akka.stream.ActorMaterializer
import annette.imc.ApsActor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import akka.http.scaladsl.model.headers._
import annette.core.AnnetteException
import annette.core.security.authentication.Session

import scala.concurrent.duration._
import annette.imc.model.{ ApStatus, _ }
import annette.imc.notification.model._
import annette.imc.notification._

import scala.util.{ Failure, Success }
import annette.imc.utils.Implicits._
import annette.core.utils.Generator
import annette.imc.user.model.UserRoled

import scala.concurrent.Future

trait CriterionRoutes
  extends NotificationConfig
  with Generator { self: APIContext with API =>

  private val updateCriterion = (path("update" / JavaUUID) & post & auth & entity(as[UpdateCriterion])) {
    (apId, sessionData, criterion) =>
      {
        val userId = sessionData.userId
        val userFuture = getUserRoled(userId).mapTo[Option[UserRoled]]

        val ff = for {

          u <- userFuture
          _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))
          a <- apsActor.ask(GetApById(apId)).mapTo[ApFound]
          _ <- predicate(a.ap.apStatus.nameMessage == ApStatus.FILLING)(new Exception("must be filling"))
          f <- apsActor.ask(UpdateCriterionCmd(apId, criterion))

        } yield f

        onComplete(ff) {
          case Success(Done) => complete("Done")
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

  private val getCriterion = (path("get" / JavaUUID / IntNumber) & get & auth) {
    (apId, cId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {
          case Success(ApFound(x)) =>
            x.criterions.get(cId) match {
              case Some(y) => complete(y.asJson)
              case None => complete("CriterionNotExists")
            }

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

  private val getList = (path("getList" / JavaUUID) & get & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {

          case Success(ApFound(x)) =>
            complete(x.criterions.values.toList.asJson)

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

  private val addFile = (path("addFile" / JavaUUID / IntNumber / JavaUUID) & get & auth) {
    (apId, cId, fileId, sessionData) =>
      {
        val userId = sessionData.userId
        val userFuture = getUserRoled(userId).mapTo[Option[UserRoled]]

        val ff = for {

          u <- userFuture
          _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))
          a <- apsActor.ask(GetApById(apId)).mapTo[ApFound]
          _ <- predicate(a.ap.apStatus.nameMessage == ApStatus.FILLING)(new Exception("must be filling"))
          f <- apsActor.ask(AddCriterionFileCmd(apId, cId, fileId))
        } yield f

        onComplete(ff) {
          case Success(Done) => complete("Done")
          case Success(CriterionNotExists) => complete("CriterionNotExists")
          case Success(FileNotExists) => complete("FileNotExists")
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

  private val getFile = (path("get" / "files" / JavaUUID / IntNumber) & get & auth) {
    (apId, cId, sessionData) =>
      {

        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {

          case Success(ApFound(x)) =>
            val apFiles = x.apFiles.values.toList
            x.criterions.get(cId) match {
              case Some(y) =>
                val cFiles = y.attachment.toList
                val files = apFiles.filter(z => cFiles.contains(z.id))
                complete(files.toList.asJson)
              case None => complete("CriterionNotExists")
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

  private val removeFile = (path("removeFile" / JavaUUID / IntNumber / JavaUUID) & get & auth) {
    (apId, cId, fileId, sessionData) =>
      {
        val userId = sessionData.userId
        val userFuture = getUserRoled(userId).mapTo[Option[UserRoled]]

        val ff = for {

          u <- userFuture
          _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))
          a <- apsActor.ask(GetApById(apId)).mapTo[ApFound]
          _ <- predicate(a.ap.apStatus.nameMessage == ApStatus.FILLING)(new Exception("must be filling"))
          f <- apsActor.ask(RemoveCriterionFileCmd(apId, cId, fileId))
        } yield f

        onComplete(ff) {
          case Success(Done) => complete("Done")
          case Success(CriterionNotExists) => complete("CriterionNotExists")
          case Success(FileNotExists) => complete("FileNotExists")
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

  private val finish = (path("finish" / JavaUUID / IntNumber) & get & auth) {
    (apId, cId, sessionData) =>
      {

        val userId = sessionData.userId
        val userFuture = getUserRoled(userId).mapTo[Option[UserRoled]]

        val ff = for {

          u <- userFuture
          _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))
          a <- apsActor.ask(GetApById(apId)).mapTo[ApFound]
          _ <- predicate(a.ap.apStatus.nameMessage == ApStatus.FILLING)(new Exception("must be filling"))
          f <- apsActor.ask(FinishCriterionCmd(apId, cId))
        } yield f
        onComplete(ff) {

          case Success(Done) => complete("Done")
          case Success(CriterionNotExists) => complete("CriterionNotExists")
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

  def criterionRoutes = pathPrefix("criterion") {
    updateCriterion ~ getCriterion ~ getList ~ addFile ~ getFile ~ removeFile ~ finish
  }
}
