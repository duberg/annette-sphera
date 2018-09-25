//package annette.imc.http.routes
//
//import java.time.LocalDate
//import java.util.UUID
//
//import akka.http.scaladsl.model.{ HttpEntity, _ }
//import akka.http.scaladsl.server.Directives.{ entity, pathPrefix, _ }
//import akka.http.scaladsl.server.Route
//import akka.pattern.ask
//import annette.imc.ApsActor
//import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
//import io.circe._
//import io.circe.generic.auto._
//import io.circe.syntax._
//
//import scala.concurrent.Future
//import annette.core.domain.tenancy.model.{ Tenant, TenantUserRole, User, UpdateUser }
//import annette.core.AnnetteException
//import annette.imc.notification.model._
//import annette.imc.notification._
//import annette.imc.user.ImcUserActor.SingleEntry
//import annette.imc.user.ImcUserActor
//import annette.imc.user.model._
//
//import scala.util.{ Failure, Success }
//import annette.imc.utils.Implicits._
//import annette.core.utils.Generator
//import annette.imc.http.Tools
//
//trait AdminRoutes
//  extends NotificationConfig
//  with Generator { self: APIContext with API =>
//
//  private val userUpdate = (path("user" / "update" / JavaUUID) & post & auth & entity(as[UpdateUser])) {
//    (id, sessionData, updateUser) =>
//
//      val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
//
//      val f = for {
//
//        u <- userFuture
//        _ <- predicate(u.exists(_.admin))(new Exception("must be in role of admin"))
//
//      } yield {
//
//        val user = UpdateUser(
//          id = id,
//          lastName = Some(updateUser.user.lastName),
//          firstName = Some(updateUser.user.firstName),
//          middleName = Some(updateUser.user.middleName),
//          email = Some(updateUser.user.email),
//          phone = Some(updateUser.user.phone),
//          locale = Some(updateUser.user.defaultLanguage))
//
//        val imcUser = ImcUser(
//          id = id,
//          sphere = updateUser.sphere.getOrElse(),
//          company = updateUser.company,
//          position = updateUser.position,
//          rank = updateUser.rank,
//          postponed = false,
//          otherTel = updateUser.otherTel,
//          otherMail = updateUser.otherMail)
//
//        coreModule.userDao.update(user)
//        imcUserActor ! ImcUserActor.UpdateCmd(imcUser)
//      }
//
//      onComplete(f) {
//
//        case Success(_) => complete("Done")
//        case Failure(throwable) =>
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//
//  }
//
//  private val userAdd = (path("user" / "add") & post & auth & entity(as[UpdateUser])) {
//    (sessionData, updateUser) =>
//
//      val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
//
//      val f = for {
//
//        u <- userFuture
//        _ <- predicate(u.exists(_.admin))(new Exception("must be in role of admin"))
//
//      } yield {
//
//        val id = UUID.randomUUID()
//
//        //val password = Tools.generatePass
//        val password = updateUser.password.getOrElse(Tools.generatePass)
//        val emailOpt = updateUser.user.email
//        val phoneOpt = updateUser.user.phone
//        val language = updateUser.user.defaultLanguage
//        val u = User(
//          id = id,
//          lastName = updateUser.user.lastName,
//          firstName = updateUser.user.firstName,
//          middleName = updateUser.user.middleName,
//          email = emailOpt,
//          phone = phoneOpt,
//          defaultLanguage = language)
//
//        coreModule.userDao.create(u, password).map(_ => coreModule.tenantUserDao.create("IMC", id))
//
//        val imcUser = ImcUser(
//          id,
//          updateUser.sphere,
//          updateUser.company,
//          updateUser.position,
//          updateUser.rank)
//
//        imcUserActor ! ImcUserActor.CreateCmd(imcUser)
//
//        emailOpt.foreach(email => {
//          for {
//            allUsers: Set[User] <- getUsersAll
//            allUserRole: Set[TenantUserRole] <- getUserRoleAll
//            allImcUsers: Map[UUID, ImcUser] <- getAllImcUsers
//          } yield {
//            val user = s"${u.lastName} ${u.firstName.take(1)}.${
//              u.middleName match {
//                case x: String if x.isEmpty => ""
//                case x: String => x.take(1) + "."
//              }
//            }"
//            val userFull = s"${u.lastName} ${u.firstName}${
//              u.middleName match {
//                case x: String if x.isEmpty => ""
//                case x: String => " " + x
//              }
//            }"
//
//            val x = for (user <- allUsers) yield {
//              val roles = allUserRole.find(_.userId == user.id).map(_.roles)
//              val imcUser = allImcUsers.get(user.id)
//
//              FullUser(
//                user.id,
//                user.lastName,
//                user.firstName,
//                user.middleName,
//                imcUser.flatMap(_.company),
//                imcUser.flatMap(_.position),
//                imcUser.flatMap(_.rank),
//                roles.exists(_.contains("admin")),
//                roles.exists(_.contains("secretar")),
//                roles.exists(_.contains("manager")),
//                roles.exists(_.contains("chairman")),
//                roles.exists(_.contains("expert")),
//                roles.exists(_.contains("additional")))
//            }
//
//            val chairman = x.find(_.chairman)
//            val chairmanName = chairman.map(x => s"${x.firstName} ${x.lastName} ${x.middleName}").getOrElse("")
//            val p = Map(
//              "Date" -> LocalDate.now(),
//              "User" -> user,
//              "UserFull" -> userFull,
//              "ChairmanOfTheExpertCouncil" -> chairmanName).mapValues(_.toString)
//
//            notificationService.addNotificationAsync(MailNotification.Password(
//              id = UUID.randomUUID(),
//              email = email,
//              password = password,
//              language = language,
//              templateParameters = p))
//          }
//        })
//
//        phoneOpt.foreach(phone => {
//          notificationService.addNotificationAsync(SmsNotification.Password(
//            id = UUID.randomUUID(),
//            phone = phone,
//            password = password,
//            language = language))
//        })
//
//      }
//
//      onComplete(f) {
//
//        case Success(_) => complete("Done")
//        case Failure(throwable) =>
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//
//  }
//
//  private val userRestore = (path("user" / "restore" / JavaUUID) & get & auth) {
//    (id, sessionData) =>
//
//      val password = Tools.generatePass
//
//      val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
//
//      val f = for {
//
//        u <- userFuture
//        _ <- predicate(u.exists(_.admin))(new Exception("must be in role of admin"))
//        _ <- coreModule.userDao.setPassword(id, password)
//        x <- coreModule.userDao.getById(id)
//
//      } yield x
//
//      onComplete(f) {
//
//        case Success(userOpt) =>
//
//          userOpt.flatMap(_.email).foreach(email => {
//            for {
//              allUsers: Set[User] <- getUsersAll
//              allUserRole: Set[TenantUserRole] <- getUserRoleAll
//              allImcUsers: Map[UUID, ImcUser] <- getAllImcUsers
//            } yield {
//              val u = userOpt.get
//
//              val user = s"${u.lastName} ${u.firstName.take(1)}.${
//                u.middleName match {
//                  case x: String if x.isEmpty => ""
//                  case x: String => x.take(1) + "."
//                }
//              }"
//              val userFull = s"${u.lastName} ${u.firstName}${
//                u.middleName match {
//                  case x: String if x.isEmpty => ""
//                  case x: String => " " + x
//                }
//              }"
//
//              val x = for (user <- allUsers) yield {
//                val roles = allUserRole.find(_.userId == user.id).map(_.roles)
//                val imcUser = allImcUsers.get(user.id)
//
//                FullUser(
//                  user.id,
//                  user.lastName,
//                  user.firstName,
//                  user.middleName.getOrElse(""),
//                  imcUser.flatMap(_.company),
//                  imcUser.flatMap(_.position),
//                  imcUser.flatMap(_.rank),
//                  roles.exists(_.contains("admin")),
//                  roles.exists(_.contains("secretar")),
//                  roles.exists(_.contains("manager")),
//                  roles.exists(_.contains("chairman")),
//                  roles.exists(_.contains("expert")),
//                  roles.exists(_.contains("additional")))
//              }
//
//              val chairman = x.find(_.chairman)
//              val chairmanName = chairman.map(x => s"${x.firstName} ${x.lastName} ${x.middleName}").getOrElse("")
//              val p = Map(
//                "Date" -> LocalDate.now(),
//                "User" -> user,
//                "UserFull" -> userFull,
//                "ChairmanOfTheExpertCouncil" -> chairmanName).mapValues(_.toString)
//
//              notificationService.addNotificationAsync(MailNotification.Password(
//                id = UUID.randomUUID(),
//                email = email,
//                password = password,
//                language = userOpt.map(_.locale.getOrElse("")).get,
//                templateParameters = p))
//            }
//          })
//
//          userOpt.flatMap(_.phone).foreach(y => {
//            notificationService.addNotificationAsync(SmsNotification.Password(
//              id = UUID.randomUUID(),
//              phone = y,
//              password = password,
//              language = userOpt.map(_.locale.getOrElse("")).get))
//          })
//
//          complete("Done")
//
//        case Failure(throwable) =>
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//  }
//
//  // /imc/api/admin/pass/restore/eMail
//
//  private val passRestore = (path("pass" / "restore" / Segment) & get) {
//    email =>
//
//      val password = Tools.generatePass
//
//      val f = for {
//
//        l <- coreModule.userDao.selectAll.mapTo[List[User]]
//        u <- l.find(e => { e.email contains email }) match {
//          case None => Future.failed(new Exception(s"E-mail ${email} not found"))
//          case Some(user) =>
//
//            Future(user)
//        }
//        _ <- { println(u.id); coreModule.userDao.setPassword(u.id, password) }
//
//      } yield u
//
//      onComplete(f) {
//
//        case Success(u) =>
//          println(u)
//          for {
//            allUsers: Set[User] <- getUsersAll
//            allUserRole: Set[TenantUserRole] <- getUserRoleAll
//            allImcUsers: Map[UUID, ImcUser] <- getAllImcUsers
//          } yield {
//
//            val user = s"${u.lastName} ${u.firstName.take(1)}.${
//              u.middleName.getOrElse("") match {
//                case x: String if x.isEmpty => ""
//                case x: String => x.take(1) + "."
//              }
//            }"
//            val userFull = s"${u.lastName} ${u.firstName}${
//              u.middleName.getOrElse("") match {
//                case x: String if x.isEmpty => ""
//                case x: String => " " + x
//              }
//            }"
//
//            val x = for (user <- allUsers) yield {
//              val roles = allUserRole.find(_.userId == user.id).map(_.roles)
//              val imcUser = allImcUsers.get(user.id)
//
//              FullUser(
//                user.id,
//                user.lastName,
//                user.firstName,
//                user.middleName.getOrElse(""),
//                imcUser.flatMap(_.company),
//                imcUser.flatMap(_.position),
//                imcUser.flatMap(_.rank),
//                roles.exists(_.contains("admin")),
//                roles.exists(_.contains("secretar")),
//                roles.exists(_.contains("manager")),
//                roles.exists(_.contains("chairman")),
//                roles.exists(_.contains("expert")),
//                roles.exists(_.contains("additional")))
//            }
//
//            val chairman = x.find(_.chairman)
//            val chairmanName = chairman.map(x => s"${x.firstName} ${x.lastName} ${x.middleName}").getOrElse("")
//            val p = Map(
//              "Date" -> LocalDate.now(),
//              "User" -> user,
//              "UserFull" -> userFull,
//              "ChairmanOfTheExpertCouncil" -> chairmanName).mapValues(_.toString)
//
//            notificationService.addNotificationAsync(MailNotification.Password(
//              id = UUID.randomUUID(),
//              email = email,
//              password = password,
//              language = u.locale.getOrElse(""),
//              templateParameters = p))
//          }
//
//          val phone = u.phone.get
//
//          notificationService.addNotificationAsync(SmsNotification.Password(
//            id = UUID.randomUUID(),
//            phone = phone,
//            password = password,
//            language = u.locale.getOrElse("")))
//
//          complete("Done")
//
//        case Failure(throwable) => // в целях безопасности не показываем ошибку
//          complete("Done")
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//  }
//
//  private val changeRole = (path("user" / "changeRole") & post & auth & entity(as[UserRoled])) {
//    (sessionData, user) =>
//
//      def addAdmin(s: Set[String]): Set[String] = {
//        if (user.admin) s + "admin"
//        else s
//      }
//      def addSecretar(s: Set[String]): Set[String] = {
//        if (user.secretar) s + "secretar"
//        else s
//      }
//      def addManager(s: Set[String]): Set[String] = {
//        if (user.manager) s + "manager"
//        else s
//      }
//      def addChairman(s: Set[String]): Set[String] = {
//        if (user.chairman) s + "chairman"
//        else s
//      }
//      def addExpert(s: Set[String]): Set[String] = {
//        if (user.expert) s + "expert"
//        else s
//      }
//      def addAdditional(s: Set[String]): Set[String] = {
//        if (user.additional) s + "additional"
//        else s
//      }
//
//      def setRoles = addAdmin _ andThen addSecretar andThen
//        addManager andThen addChairman andThen addExpert andThen addAdditional
//
//      val roles: Set[String] = setRoles(Set.empty[String])
//
//      val userRole = TenantUserRole(
//        "IMC",
//        user.id,
//        roles)
//
//      val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
//
//      val f = for {
//
//        u <- userFuture
//        _ <- predicate(u.exists(_.admin))(new Exception("must be in role of admin"))
//        x <- coreModule.tenantUserRoleDao.store(userRole)
//      } yield x
//
//      onComplete(f) {
//        case Success(x) =>
//          complete(x)
//        case Failure(throwable) =>
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//
//  }
//  private val userRemove = (path("user" / "remove" / JavaUUID) & get & auth) {
//    (id, sessionData) =>
//      val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
//
//      val f = for {
//
//        u <- userFuture
//        _ <- predicate(u.exists(_.admin))(new Exception("must be in role of admin"))
//        x <- coreModule.userDao.delete(id)
//
//      } yield x
//
//      onComplete(f) {
//        case Success(x) =>
//          val f1 = imcUserActor ! ImcUserActor.DeleteCmd(id)
//          complete(x)
//        case Failure(throwable) =>
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//
//  }
//
//  private val changeManager = (path("manager" / "change" / JavaUUID / JavaUUID) & get & auth) {
//    (ap, pm, sessionData) =>
//      val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
//
//      val f = for {
//
//        u <- userFuture
//        _ <- predicate(u.exists(_.admin))(new Exception("must be in role of admin"))
//        x <- ask(apsActor, ApsActor.ChangeManagerCmd(ap, pm))
//
//      } yield x
//
//      onComplete(f) {
//        case Success(x) =>
//          complete("Done")
//        case Failure(throwable) =>
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//
//  }
//
//  private val getUser = (path("user" / "get" / JavaUUID) & get & auth) {
//    (id, sessionData) =>
//
//      val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
//
//      val z = for {
//        ur <- userFuture
//        _ <- predicate(ur.exists(x => x.admin || x.chairman))(new Exception("must be in role of admin or chairman"))
//        u <- imcUserActor.ask(ImcUserActor.GetById(id)).mapTo[SingleEntry].map(_.maybeEntry)
//        f <- coreModule.userDao.getById(id)
//      } yield (f, u)
//
//      val ff: Future[Option[UpdatedUser]] = z.map { d =>
//        d._2 match {
//          case Some(b) =>
//            for {
//              a <- d._1
//              b <- d._2
//            } yield {
//              UpdatedUser(
//                id,
//                PreUser(
//                  a.lastName, a.firstName, a.middleName.getOrElse(""), a.email, a.phone, a.locale.getOrElse("")),
//                None,
//                b.sphere,
//                b.company,
//                b.position,
//                b.rank,
//                b.otherTel,
//                b.otherMail)
//            }
//          case None => for {
//            a <- d._1
//            b <- Some(ImcUser(id, None, None, None, None))
//          } yield {
//            UpdatedUser(
//              id,
//              PreUser(
//                a.lastName, a.firstName, a.middleName.getOrElse(""), a.email, a.phone, a.locale.getOrElse("")),
//              None,
//              b.sphere,
//              b.company,
//              b.position,
//              b.rank,
//              b.otherTel,
//              b.otherMail)
//          }
//        }
//      }
//
//      onComplete(ff) {
//        case Success(Some(x)) => complete(x.asJson)
//        case Success(None) => complete("It's bad")
//        case Success(_) => complete(StatusCodes.InternalServerError)
//        case Failure(throwable) =>
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//  }
//
//  private val getAllUsers = (pathPrefix("user" / "all") & get & auth) {
//    sessionData =>
//      pathEnd {
//
//        val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
//        val ff = for {
//          //          u <- userFuture
//          //          _ <- predicate(u.exists(x => x.admin || x.chairman))(new Exception("must be in role of admin or chairman"))
//          f <- coreModule.userDao.selectAll
//          r <- coreModule.tenantUserRoleDao.selectAll
//        } yield (f, r)
//
//        onComplete(ff) {
//          case Success((x, y)) =>
//
//            val z = x.map { user =>
//              val roles = y.find(_.userId == user.id).map(_.roles)
//
//              UserRoled(
//                user.id,
//                user.lastName,
//                user.firstName,
//                user.middleName.getOrElse(),
//                user.email,
//                roles.exists(_.contains("admin")),
//                roles.exists(_.contains("secretar")),
//                roles.exists(_.contains("manager")),
//                roles.exists(_.contains("chairman")),
//                roles.exists(_.contains("expert")),
//                roles.exists(_.contains("additional")))
//            }
//
//            complete(z.asJson)
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
//
//  private val getManagers = (pathPrefix("user" / "managers") & get & auth) {
//    sessionData =>
//      val userFuture = getUserRoled(sessionData.userId).mapTo[Option[UserRoled]]
//      val ff = for {
//        u <- userFuture
//        _ <- predicate(u.exists(x => x.admin || x.chairman))(new Exception("must be in role of admin or chairman"))
//        f <- coreModule.userDao.selectAll
//        r <- coreModule.tenantUserRoleDao.selectAll
//      } yield (f, r)
//
//      onComplete(ff) {
//        case Success((x, y)) =>
//
//          val z = x.map { user =>
//            val roles = y.find(_.userId == user.id).map(_.roles)
//
//            UserRoled(
//              user.id,
//              user.lastName,
//              user.firstName,
//              user.middleName.getOrElse(""),
//              user.email,
//              roles.exists(_.contains("admin")),
//              roles.exists(_.contains("secretar")),
//              roles.exists(_.contains("manager")),
//              roles.exists(_.contains("chairman")),
//              roles.exists(_.contains("expert")),
//              roles.exists(_.contains("additional")))
//          }.filter(_.manager)
//
//          complete(z.asJson)
//        case Success(_) => complete(StatusCodes.InternalServerError)
//        case Failure(throwable) =>
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//  }
//
//  private val currentUser = (path("user" / "current") & get & auth) {
//    sessionData =>
//
//      val ff = for {
//        f <- coreModule.userDao.getById(sessionData.userId)
//        r <- coreModule.tenantUserRoleDao.getByIds(sessionData.tenantId, sessionData.userId)
//      } yield (f, r)
//
//      onComplete(ff) {
//        case Success((x, y)) =>
//
//          val z = x.map { user =>
//            val roles = y.map(_.roles)
//
//            UserRoled(
//              user.id,
//              user.lastName,
//              user.firstName,
//              user.middleName.getOrElse(""),
//              user.email,
//              roles.exists(_.contains("admin")),
//              roles.exists(_.contains("secretar")),
//              roles.exists(_.contains("manager")),
//              roles.exists(_.contains("chairman")),
//              roles.exists(_.contains("expert")),
//              roles.exists(_.contains("additional")))
//          }
//
//          complete(z.asJson)
//        case Success(_) => complete(StatusCodes.InternalServerError)
//        case Failure(throwable) =>
//          throwable match {
//            case annetteException: AnnetteException =>
//              complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
//            case _ =>
//              complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
//          }
//      }
//
//  }
//
//  def adminRoutes: Route = pathPrefix("admin") {
//    userUpdate ~ userAdd ~ userRestore ~ changeRole ~ userRemove ~
//      changeManager ~ getUser ~ getAllUsers ~ getManagers ~ currentUser ~ passRestore
//  }
//}
