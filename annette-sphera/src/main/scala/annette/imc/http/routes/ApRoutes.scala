package annette.imc.http.routes

import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives.{ entity, pathPrefix, _ }
import akka.pattern.ask
import annette.core.{ AnnetteException, CoreModule }
import annette.imc.ApsActor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import akka.http.scaladsl.model.headers._
import annette.core.domain.tenancy.model.TenantUserRole
import annette.imc.model.{ ApStatus, _ }

import scala.util.{ Failure, Success }
import annette.imc.utils.Implicits._
import annette.core.utils.Generator
import annette.imc.user.model.UserRoled

import scala.concurrent.Future

trait ApRoutes
  //  extends NotificationConfig
  extends Generator { self: APIContext with API =>

  val coreModule: CoreModule

  //  private val newAp = (path("new") & get & auth) {
  //    sessionData =>
  //      {
  //        val userId = sessionData.userId
  //
  //        val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
  //
  //        val ff = for {
  //
  //          u <- userFuture
  //          _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))
  //
  //          d <- apsActor.ask(CreateCmd(userId)).mapTo[Created]
  //          f <- coreModule.tenantUserRoleDao.selectAll.mapTo[List[TenantUserRole]]
  //        } yield {
  //          val expertList = f.filter(x => x.roles.contains("expert") || x.roles.contains("additional"))
  //          (d, expertList)
  //        }
  //        onComplete(ff) {
  //          case Success((Created(id), experts)) => {
  //
  //            experts.foreach(x => {
  //              apsActor ! AddExpertCmd(id, x.userId)
  //            })
  //            complete(id.asJson)
  //          }
  //          case Success(_) => complete(StatusCodes.InternalServerError)
  //          case Failure(throwable) =>
  //            throwable match {
  //              case annetteException: AnnetteException =>
  //                complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
  //              case _ =>
  //                complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
  //            }
  //        }
  //      }
  //  }

  private val newAp1 = (path("new1") & get & auth) { //только для тестирования
    sessionData =>
      {
        val userId = sessionData.userId

        val f = apsActor.ask(CreateCmd(userId))
        onComplete(f) {
          case Success(Created(id)) => {

            complete(id.asJson)
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

  private val removeAp = (path("remove" / JavaUUID) & get & auth) {
    (apId, sessionData) =>
      // админ может удалить заявки
      {
        val userId = sessionData.userId

        val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]

        val f = for {

          u <- userFuture
          _ <- predicate(u.exists(_.admin))(new Exception("must be in role of admin"))
          a <- apsActor.ask(GetApById(apId)).mapTo[ApFound].map(_.ap.apFiles.keys.toSeq)
          _ <- Future(a.foreach(fileId => {
            val file = ctx.fileStorageDir + "/" + fileId
            deleteFile(file)
          }))
          ff <- apsActor.ask(RemoveCmd(apId))
        } yield ff

        onComplete(f) {
          case Success(Done) => complete("Done")
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

  private val removeNew = (path("remove-new" / JavaUUID) & get & auth) {

    // проектный менеджер удаляет заявку, пока она на заполнении
    (apId, sessionData) =>
      {
        val userId = sessionData.userId

        val userFuture = getUserRoled(userId).mapTo[Option[UserRoled]]

        val f = for {

          u <- userFuture
          _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))

          a <- apsActor.ask(GetApById(apId)).mapTo[ApFound]
          _ <- predicate(a.ap.apStatus.nameMessage == ApStatus.FILLING)(new Exception("must be filling"))
          _ <- Future(a.ap.apFiles.keys.toSeq.foreach(fileId => {
            val file = ctx.fileStorageDir + "/" + fileId
            deleteFile(file)
          }))
          ff <- apsActor.ask(RemoveCmd(apId))
        } yield ff

        onComplete(f) {
          case Success(Done) => complete("Done")
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

  private val update = (path("update") & post & auth & entity(as[UpdateAp])) {
    (sessionData, up) =>
      {
        val userId = sessionData.userId
        val userFuture = getUserRoled(userId).mapTo[Option[UserRoled]]

        val f = for {

          u <- userFuture
          _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))
          x <- apsActor.ask(FillingFormCmd(up))
        } yield x

        onComplete(f) {
          case Success(Done) => complete("Done".asJson)
          case Success(ApNotExists) => complete("ApNotExists".asJson)

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

  private val getAll = (path("all") & get & auth) {
    sessionData =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(FindAps(SearchParams()))
        onComplete(f) {
          case Success(ApsFound(x)) =>

            complete(x.map(a => {
              val name = a.apData.name.getOrElse(ApString("", ""))
              val status = a.apStatus.nameMessage
              ApSimple(a.id, Some(name.ru), Some(name.en), status, a.projectManager.toString).asJson
            }))
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

  private val getFilled = (path("filled") & get & auth) {
    sessionData =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(FindAps(SearchParams()))
        onComplete(f) {
          case Success(ApsFound(x)) =>

            complete(x.filter(_.apStatus.nameMessage != "filling").map(a => {
              val name = a.apData.name.getOrElse(ApString("", ""))
              val status = a.apStatus.nameMessage
              ApSimple(a.id, Some(name.ru), Some(name.en), status, a.projectManager.toString).asJson
            }))
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

  private val getReady = (path("ready") & get & auth) {
    sessionData =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(FindAps(SearchParams()))
        onComplete(f) {
          case Success(ApsFound(x)) =>

            complete(x.filter(z => z.apStatus.nameMessage != "filling" &&
              z.apStatus.nameMessage != "filled").map(a => {
              val name = a.apData.name.getOrElse(ApString("", ""))
              val status = a.apStatus.nameMessage
              ApSimple(a.id, Some(name.ru), Some(name.en), status, a.projectManager.toString).asJson
            }))
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

  private val getFilling = (path("filling") & get & auth) {
    sessionData =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(FindAps(SearchParams()))
        onComplete(f) {
          case Success(ApsFound(x)) =>

            complete(x.filter(s => s.apStatus.nameMessage == "filling" || s.apStatus.nameMessage == "accomplished").map(a => {
              val name = a.apData.name.getOrElse(ApString("", ""))
              val status = a.apStatus.nameMessage
              ApSimple(a.id, Some(name.ru), Some(name.en), status, a.projectManager.toString).asJson
            }))
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

  private val getForExpert = (path("allexperts") & get & auth) {
    sessionData =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(FindAps(SearchParams()))
        onComplete(f) {
          case Success(ApsFound(x)) =>

            complete(x.filter(y => (y.apStatus.nameMessage == "onexpertise" || y.apStatus.nameMessage == "accomplished") && y.expertise.experts.contains(userId)).map(a => {
              val name = a.apData.name.getOrElse(ApString("", ""))
              val status = a.apStatus.nameMessage
              ApSimple(a.id, Some(name.ru), Some(name.en), status, a.projectManager.toString).asJson
            }))
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

  private val getUpdateAp = (path("get" / "update" / JavaUUID) & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {
          case Success(ApFound(x)) =>
            val u = UpdateAp(
              id = x.id,
              entityName = x.apData.entityName,
              personName = x.apData.personName,
              personPosition = x.apData.personPosition,
              personEmail = x.apData.personEmail,
              personTel = x.apData.personTel,
              country = x.apData.country,
              operationTypes = x.apData.operationTypes,
              financing = x.apData.financing,
              isForLong = x.apData.isForLong,
              purpose = x.apData.purpose,
              name = x.apData.name,
              capital = x.apData.capital,
              applicantInfo = x.apData.applicantInfo,
              address = x.apData.address)

            complete(u.asJson)
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

  private val getFiles = (path("get" / "files" / JavaUUID) & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {
          case Success(ApFound(x)) =>
            val u = x.apFiles.values
            complete(u.asJson)
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

  def apRoutes = pathPrefix("ap") {
    newAp1 ~ removeAp ~ removeNew ~ update ~ getAll ~ getFilled ~
      getReady ~ getFilling ~ getForExpert ~ getUpdateAp ~ getFiles
  }

  //newAp ~

}
