package annette.imc.http.routes

import java.time.{ LocalDate, ZonedDateTime }
import java.util.UUID
import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives.{ entity, pathPrefix, _ }
import akka.pattern.ask
import annette.imc.ApsActor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import scala.concurrent.Future
import annette.core.domain.tenancy.model.{ TenantUserRole, User }
import annette.core.exception.AnnetteException
import annette.imc.model.{ ApStatus, _ }
import annette.imc.notification.model._
import annette.imc.notification._
import annette.imc.user.model._
import scala.util.{ Failure, Success }
import annette.imc.utils.Implicits._
import annette.core.utils.Generator

trait StatusRoutes
  extends NotificationConfig
  with Generator { self: APIContext with API =>

  private val ready = (path("ready" / JavaUUID) & get & auth) {
    // посылается секретарем председателю
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(ChangeStatusCmd(apId, ApStatus(ApStatus.READY, ZonedDateTime.now(), Some(userId))))
        onComplete(f) {
          case Success(Done) =>

            case class UserWithRoles(u: User, isSecretar: Boolean, isChairman: Boolean)

            for {
              allUsers <- coreModule.userDao.selectAll.map(_.toSet)
              allUserRole <- coreModule.tenantUserRoleDao.selectAll.map(_.toSet)
              ap <- getApById(apId)
            } yield {
              val x = for (user <- allUsers) yield {
                val roles = allUserRole.find(_.userId == user.id).map(_.roles)
                UserWithRoles(
                  u = user,
                  isSecretar = roles.exists(_.contains("secretar")),
                  isChairman = roles.exists(_.contains("chairman")))
              }
              val secretar = x.find(_.isSecretar)
              val chairman = x.find(_.isChairman)

              val chairmanName = chairman.map(x => s"${x.u.firstName} ${x.u.lastName} ${x.u.middleName.getOrElse("")}").getOrElse("")

              //                secretar.foreach {
              //                  case UserWithRoles(u, _, _) =>
              //                    u.email.foreach { email =>
              //                      val userName = s"${u.lastName} ${u.firstName.take(1)}.${
              //                        u.middleName match {
              //                          case x: String if x.isEmpty => ""
              //                          case x: String => x.take(1) + "."
              //                        }
              //                      }"
              //                      val userNameFull = s"${u.lastName} ${u.firstName}${
              //                        u.middleName match {
              //                          case x: String if x.isEmpty => ""
              //                          case x: String => " " + x
              //                        }
              //                      }"
              //
              //                      val applicantName = applicant(ap, u.locale)
              //
              //                      val p = Map(
              //                        "Id" -> apId,
              //                        "Date" -> LocalDate.now(),
              //                        "User" -> userName,
              //                        "UserFull" -> userNameFull,
              //                        "Applicant" -> applicantName,
              //                        "ChairmanOfTheExpertCouncil" -> chairmanName).mapValues(_.toString)
              //
              //                      notificationService.addNotificationAsync(MailNotification.ToReview(
              //                        id = UUID.randomUUID(),
              //                        email = email,
              //                        language = u.locale,
              //                        templateParameters = p))
              //                    }
              //                    u.phone.foreach { phone =>
              //                      notificationService.addNotificationAsync(SmsNotification.ToReview(
              //                        id = UUID.randomUUID(),
              //                        phone = phone,
              //                        language = u.locale))
              //                    }
              //                }

              chairman.foreach {
                case UserWithRoles(u, _, _) =>

                  u.email.foreach { email =>
                    val userName = s"${u.lastName} ${u.firstName.take(1)}.${
                      u.middleName.getOrElse("") match {
                        case x: String if x.isEmpty => ""
                        case x: String => x.take(1) + "."
                      }
                    }"
                    val userNameFull = s"${u.lastName} ${u.firstName}${
                      u.middleName.getOrElse("") match {
                        case x: String if x.isEmpty => ""
                        case x: String => " " + x
                      }
                    }"

                    val applicantName = applicant(ap, u.language.getOrElse(""))

                    val p = Map(
                      "Id" -> apId,
                      "Date" -> LocalDate.now(),
                      "User" -> userName,
                      "UserFull" -> userNameFull,
                      "Positions" -> "Председателю",
                      "Applicant" -> applicantName,
                      "ChairmanOfTheExpertCouncil" -> chairmanName).mapValues(_.toString)

                    notificationService.addNotificationAsync(MailNotification.ToReview(
                      id = UUID.randomUUID(),
                      email = email,
                      language = u.language.getOrElse(""),
                      templateParameters = p))
                  }

                  u.phone.foreach { phone =>
                    notificationService.addNotificationAsync(SmsNotification.ToReview(
                      id = UUID.randomUUID(),
                      phone = phone,
                      language = u.language.getOrElse("")))
                  }
              }
            }

            complete("Done")
          case Success(ApNotExists) => complete("ApNotExists")
          case Success(EnterEntityNameFirst) => complete("EnterEntityNameFirst")
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

  private val filled = (path("filled" / JavaUUID) & get & auth) {
    // посылается проектным менеджером секретарю
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(ChangeStatusCmd(apId, ApStatus(ApStatus.FILLED, ZonedDateTime.now(), Some(userId))))
        onComplete(f) {
          case Success(Done) =>

            case class UserWithRoles(u: User, isSecretar: Boolean, isChairman: Boolean)

            for {
              allUsers <- coreModule.userDao.selectAll.map(_.toSet)
              allUserRole <- coreModule.tenantUserRoleDao.selectAll.map(_.toSet)
              ap <- getApById(apId)
            } yield {
              val x = for (user <- allUsers) yield {
                val roles = allUserRole.find(_.userId == user.id).map(_.roles)
                UserWithRoles(
                  u = user,
                  isSecretar = roles.exists(_.contains("secretar")),
                  isChairman = roles.exists(_.contains("chairman")))
              }
              val secretar = x.find(_.isSecretar)
              val chairman = x.find(_.isChairman)

              val chairmanName = chairman.map(x => s"${x.u.firstName} ${x.u.lastName} ${x.u.middleName.getOrElse("")}").getOrElse("")

              secretar.foreach {
                case UserWithRoles(u, _, _) =>
                  u.email.foreach { email =>
                    val userName = s"${u.lastName} ${u.firstName.take(1)}.${
                      u.middleName.getOrElse("") match {
                        case x: String if x.isEmpty => ""
                        case x: String => x.take(1) + "."
                      }
                    }"
                    val userNameFull = s"${u.lastName} ${u.firstName}${
                      u.middleName.getOrElse("") match {
                        case x: String if x.isEmpty => ""
                        case x: String => " " + x
                      }
                    }"

                    val applicantName = applicant(ap, u.language.getOrElse(""))

                    val p = Map(
                      "Id" -> apId,
                      "Date" -> LocalDate.now(),
                      "User" -> userName,
                      "UserFull" -> userNameFull,
                      "Positions" -> "Секретарю",
                      "Applicant" -> applicantName,
                      "ChairmanOfTheExpertCouncil" -> chairmanName).mapValues(_.toString)

                    notificationService.addNotificationAsync(MailNotification.ToReview(
                      id = UUID.randomUUID(),
                      email = email,
                      language = u.language.getOrElse(""),
                      templateParameters = p))
                  }
                  u.phone.foreach { phone =>
                    notificationService.addNotificationAsync(SmsNotification.ToReview(
                      id = UUID.randomUUID(),
                      phone = phone,
                      language = u.language.getOrElse("")))
                  }
              }

              //              chairman.foreach {
              //                case UserWithRoles(u, _, _) =>
              //
              //                  u.email.foreach { email =>
              //                    val userName = s"${u.lastName} ${u.firstName.take(1)}.${
              //                      u.middleName match {
              //                        case x: String if x.isEmpty => ""
              //                        case x: String => x.take(1) + "."
              //                      }
              //                    }"
              //                    val userNameFull = s"${u.lastName} ${u.firstName}${
              //                      u.middleName match {
              //                        case x: String if x.isEmpty => ""
              //                        case x: String => " " + x
              //                      }
              //                    }"
              //
              //                    val applicantName = applicant(ap, u.locale)
              //
              //                    val p = Map(
              //                      "Id" -> apId,
              //                      "Date" -> LocalDate.now(),
              //                      "User" -> userName,
              //                      "UserFull" -> userNameFull,
              //                      "Applicant" -> applicantName,
              //                      "ChairmanOfTheExpertCouncil" -> chairmanName).mapValues(_.toString)
              //
              //                    notificationService.addNotificationAsync(MailNotification.ToReview(
              //                      id = UUID.randomUUID(),
              //                      email = email,
              //                      language = u.locale,
              //                      templateParameters = p))
              //                  }
              //
              //                  u.phone.foreach { phone =>
              //                    notificationService.addNotificationAsync(SmsNotification.ToReview(
              //                      id = UUID.randomUUID(),
              //                      phone = phone,
              //                      language = u.locale))
              //                  }
              //              }
            }

            complete("Done")
          case Success(ApNotExists) => complete("ApNotExists")
          case Success(EnterEntityNameFirst) => complete("EnterEntityNameFirst")
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

  private val notReadyPost = (path("notReady" / JavaUUID) & post & auth & entity(as[ApComment])) {

    (apId, sessionData, comment) =>
      {
        val userId = sessionData.userId

        val f = for {
          _ <- apsActor.ask(ChangeStatusCmd(apId, ApStatus(ApStatus.FILLING, ZonedDateTime.now(), Some(userId), Some(comment.comment))))
          a <- apsActor.ask(GetApById(apId)).mapTo[ApFound].map(_.ap)
          name <- Future(a.apData.name.map(_.ru).getOrElse("Наименование на русском не указано"))
          id <- Future(a.projectManager)
          u <- coreModule.userDao.getById(id)
          sender <- coreModule.userDao.getById(userId)
        } yield (name, u, sender)

        onComplete(f) {
          case Success((name, Some(pm), Some(sender))) =>
            val u = pm.asInstanceOf[User]
            val s = sender.asInstanceOf[User]
            val user = s"${u.lastName} ${u.firstName.take(1)}.${
              u.middleName.getOrElse("") match {
                case x: String if x.isEmpty => ""
                case x: String => x.take(1) + "."
              }
            }"
            val userFull = s"${u.lastName} ${u.firstName}${
              u.middleName.getOrElse("") match {
                case x: String if x.isEmpty => ""
                case x: String => " " + x
              }
            }"

            val senderShort = s"${s.lastName} ${s.firstName.take(1)}.${
              s.middleName.getOrElse("") match {
                case x: String if x.isEmpty => ""
                case x: String => x.take(1) + "."
              }
            }"

            val p = Map(
              "Date" -> LocalDate.now(),
              "Applicant" -> name,
              "Comments" -> comment.comment,
              "User" -> user,
              "UserFull" -> userFull,
              "Sender" -> senderShort).mapValues(_.toString)

            notificationService.addNotificationAsync(MailNotification.NotReady(
              id = UUID.randomUUID(),
              email = u.email.getOrElse(""),
              language = "RU",
              templateParameters = p))

            complete("Done")

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

  private val notReady = (path("notReady" / JavaUUID) & get & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(ChangeStatusCmd(apId, ApStatus(ApStatus.FILLING, ZonedDateTime.now(), Some(userId), None)))
        onComplete(f) {
          case Success(Done) => complete("Done")
          case Success(ApNotExists) => complete("ApNotExists")
          case Success(EnterEntityNameFirst) => complete("EnterEntityNameFirst")
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

  private val toExpertise = (path("toExpertise" / JavaUUID) & get & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(ChangeStatusCmd(apId, ApStatus(ApStatus.ONEXPERTISE, ZonedDateTime.now(), Some(userId), None)))
        onComplete(f) {
          case Success(Done) =>
            // ========= Notifications =========
            for {
              expertIds <- apsActor.ask(GetApById(apId))
                .mapTo[ApFound]
                .map(_.ap.expertise.experts)
              experts <- coreModule.userDao.selectAll.map(_.filter(expertIds contains _.id))
              allUsers: Set[User] <- getUsersAll
              allUserRole: Set[TenantUserRole] <- getUserRoleAll
              allImcUsers: Map[UUID, ImcUser] <- getAllImcUsers
              ap <- getApById(apId)
            } yield {
              experts.foreach { u =>
                val language = u.language

                u.email.foreach { email =>
                  val expert = s"${u.lastName} ${u.firstName.take(1)}.${
                    u.middleName.getOrElse("") match {
                      case x: String if x.isEmpty => ""
                      case x: String => x.take(1) + "."
                    }
                  }"
                  val expertFull = s"${u.lastName} ${u.firstName}${
                    u.middleName.getOrElse("") match {
                      case x: String if x.isEmpty => ""
                      case x: String => " " + x
                    }
                  }"

                  val applicantName = applicant(ap, u.language.getOrElse("RU"))

                  val x = for (user <- allUsers) yield {
                    val roles = allUserRole.find(_.userId == user.id).map(_.roles)
                    val imcUser = allImcUsers.get(user.id)

                    FullUser(
                      user.id,
                      user.lastName,
                      user.firstName,
                      user.middleName.getOrElse(""),
                      imcUser.flatMap(_.company),
                      imcUser.flatMap(_.position),
                      imcUser.flatMap(_.rank),
                      roles.exists(_.contains("admin")),
                      roles.exists(_.contains("secretar")),
                      roles.exists(_.contains("manager")),
                      roles.exists(_.contains("chairman")),
                      roles.exists(_.contains("expert")),
                      roles.exists(_.contains("additional")))
                  }

                  val chairman = x.find(_.chairman)
                  val chairmanName = chairman.map(x => s"${x.lastName} ${x.firstName} ${x.middleName}").getOrElse("")

                  val p = Map(
                    "Id" -> apId,
                    "Date" -> LocalDate.now(),
                    "User" -> expert,
                    "UserFull" -> expertFull,
                    "Applicant" -> applicantName,
                    "ChairmanOfTheExpertCouncil" -> chairmanName).mapValues(_.toString)

                  notificationService.addNotificationAsync(MailNotification.ToExpertise(
                    id = UUID.randomUUID(),
                    email = email,
                    language = language.getOrElse(""),
                    templateParameters = p))
                }
                u.phone.foreach { phone =>
                  notificationService.addNotificationAsync(SmsNotification.ToExpertise(
                    id = UUID.randomUUID(),
                    phone = phone,
                    language = language.getOrElse("")))
                }
              }
              experts
            }

            complete("Done")
          case Success(ApNotExists) => complete("ApNotExists")
          case Success(EnterEntityNameFirst) => complete("EnterEntityNameFirst")
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

  private val accomplishedPost = (path("accomplished" / JavaUUID) & post & auth & entity(as[ApResult])) {
    (apId, sessionData, result) =>
      {
        val userId = sessionData.userId

        val f = apsActor.ask(ChangeStatusCmd(
          apId,
          ApStatus(
            ApStatus.ACCOMPLISHED,
            ZonedDateTime.now(),
            Some(userId),
            None,
            Some(result))))
        onComplete(f) {
          case Success(Done) => complete("Done")
          case Success(ApNotExists) => complete("ApNotExists")
          case Success(EnterEntityNameFirst) => complete("EnterEntityNameFirst")
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

  private val accomplished = (path("accomplished" / JavaUUID) & get & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(ChangeStatusCmd(
          apId,
          ApStatus(
            ApStatus.ACCOMPLISHED,
            ZonedDateTime.now(),
            Some(userId),
            None,
            None)))
        onComplete(f) {
          case Success(Done) => complete("Done")
          case Success(ApNotExists) => complete("ApNotExists")
          case Success(EnterEntityNameFirst) => complete("EnterEntityNameFirst")
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

  private val getStatus = (path("get" / JavaUUID) & get & auth) {
    (apId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {
          case Success(ApFound(x)) =>
            complete(x.apStatus.asJson)
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

  def statusRoutes = pathPrefix("status") {

    ready ~ filled ~ notReadyPost ~ notReady ~ toExpertise ~ accomplishedPost ~ accomplished ~ getStatus
  }

}
