package annette.core.security.authentication

import java.util.UUID

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.event.LoggingReceive
import akka.http.scaladsl.util.FastFuture
import annette.core.domain.application.ApplicationManager
import annette.core.security.authentication.AuthenticationService.{ FailureResponse, Login }
import annette.core.security.authentication.jwt.JwtHelper
import annette.core.domain.application._
import annette.core.domain.language.dao.LanguageDao
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.dao._
import annette.core.domain.tenancy.model.{ OpenSession, Tenant, User }
import org.joda.time.DateTime

import scala.concurrent.Future

class LoginActor(
  userDao: UserService,
  sessionDao: SessionDao,
  tenantDao: TenantDao,
  tenantUserDao: TenantUserDao,
  applicationDao: ApplicationManager,
  languageDao: LanguageDao,
  rememberMeSessionTimeout: Int,
  sessionTimeout: Int,
  override val secret: String)
  extends Actor with ActorLogging with JwtHelper {

  implicit val ec = context.dispatcher

  def validateUser(login: String, password: String) = {
    for {
      userOpt <- userDao.getByLoginAndPassword(login, password)
    } yield {
      context.system.log.debug(s"validateUser: $userOpt")
      userOpt.getOrElse(throw new AuthenticationFailedException())
    }
  }

  override def receive: Receive = LoggingReceive {
    case msg: AuthenticationService.Login =>
      if (msg.loginData.selectTenant) provideUserTenantData(sender, msg, None)
      else login(msg)
  }

  /* ----------------------- Login ----------------------- */

  def login(msg: Login) = {
    val requestor = sender

    context.system.log.debug(msg.toString)

    val future = for {
      // 1. Проверки пользователя:
      user <- validateUser(msg.loginData.login, msg.loginData.password)

      userTenants <- tenantUserDao.getUserTenantData(user.id)
      // получить организацию и приложение для входа
      (tenantId, applicationId) <- {
        val r = getTenantAndApplication(user.id, userTenants, msg.loginData.tenant, msg.loginData.application)
        r.foreach { case (tenantId, applicationId) => context.system.log.debug(s"getTenantAndApplication: tenantId: $tenantId, applicationId: $applicationId") }
        r.failed.foreach(th => context.system.log.debug(s"getTenantAndApplication: ${th.getMessage}"))
        r
      }
      // 4. Если все проверки пользователя пройдены, то при последующих ошибках проверки
      // пользователю предлагается выбрать организацию/приложение

      // 5. Проверки организации:
      tenant <- tenantDao
        .getById(tenantId)
        .map(_.getOrElse(throw new TenantNotFoundException()))

      // Проверки приложения в организации
      _ = if (!tenant.applications.contains(applicationId)) throw new ApplicationNotAssignedToTenantException()

      // Проверки языка в организации
      languageId = msg.loginData.language.getOrElse(tenant.defaultLanguageId)
      _ = if (!tenant.languages.contains(languageId)) throw new LanguageNotAssignedToTenantException()

      // 6. Проверки пользователя в организации:
      tenantUser <- tenantUserDao
        .getByIds(tenantId, user.id)
        .map(_.getOrElse(throw new UserNotAssignedToTenantException()))

      // 7. Проверки приложения:
      application <- applicationDao
        .getById(applicationId)
        .map(_.getOrElse(throw new ApplicationNotFoundException()))

      // Проверки языка:
      language <- languageDao
        .getById(languageId)
        .map(_.getOrElse(throw new LanguageNotFoundException()))

      languages <- languageDao.selectAll

      // 8. Если все проверки прошли создаём сессию:
      openSession <- createOpenSession(tenant, user, application, language, msg.loginData.rememberMe, msg.ip)
    } yield {
      val sessionData = Session(
        openSession.id,
        openSession.userId,
        openSession.tenantId,
        openSession.applicationId,
        openSession.languageId)
      val jwtToken = encodeSessionData(sessionData)
      ApplicationState(
        authenticated = true,
        language = language,
        languages = languages,
        user = Some(user),
        tenant = Some(tenant),
        application = Some(application),
        tenantData = userTenants,
        jwtToken = Some(jwtToken))

    }

    future.foreach { case res => requestor ! res }
    future.failed.foreach {
      case forbiddenException: ForbiddenException =>
        provideUserTenantData(requestor, msg, Some(forbiddenException))
      // если выявлена ошибка аутентификации, то отправляем эту ошибку
      case authenticationException: AuthenticationException =>
        requestor ! FailureResponse(authenticationException)
      // при любой другой ошибке отправляем ошибку аутентификации
      case throwable: Throwable =>
        requestor ! FailureResponse(new AuthenticationFailedException())
    }
  }

  def getTenantAndApplication(
    userId: User.Id,
    tenantData: scala.Seq[TenantData],
    maybeTenant: Option[Tenant.Id],
    maybeApplication: Option[Application.Id]): Future[(Tenant.Id, Application.Id)] = {
    context.system.log.debug(s"getTenantAndApplication: maybeTenant: $maybeTenant, maybeApplication: $maybeApplication, tenantData: $tenantData")
    if (maybeTenant.isDefined &&
      maybeApplication.isDefined &&
      tenantData.exists(t => t.id == maybeTenant.get && t.apps.map(_.id).contains(maybeApplication.get))) {
      FastFuture.successful((maybeTenant.get, maybeApplication.get))
    } else {
      // 2. Если не указан id организации, то выполняется вход в организацию/приложение из последней сессии
      for {
        lastSessionOpt <- sessionDao.getLastSessionByUserId(userId)
      } yield lastSessionOpt
        .map(sh => (sh.tenantId, sh.applicationId))
        .getOrElse {
          if (tenantData.length == 1 && tenantData.head.apps.length == 1) {
            // Если пользователь присвоен одной организации и одному приложению то используем эти данные
            (tenantData.head.id, tenantData.head.apps.head.id)
          } else {
            throw new TenantAndApplicationNotFoundException()
          }
        }
    }
  }

  def createOpenSession(tenant: Tenant, user: User, application: Application, language: Language, rememberMe: Boolean, ip: String): Future[OpenSession] = {
    val timeout = if (rememberMe) {
      // 1. Если установлен флаг Запомнить меня, то длительность сессии равна значению
      // параметра rememberMeSessionTimeout установленного в настройках приложения (в минутах, по умолчанию 2 недели).
      // Таймаут сессии происходит в момент Начало сессии + Длительность сессии
      rememberMeSessionTimeout
    } else {
      // 2. Если не установлен флаг Запомнить меня, то длительность сессии равна значению
      // параметра sessionTimeout установленного в настройках приложения (в минутах, по умолчанию 1 час).
      // Таймаут сессии происходит в момент Последнее обращение к серверу + Длительность сессии
      sessionTimeout
    }
    // 2. создаётся запись открытой сессии
    val openSession = OpenSession(
      user.id,
      tenant.id,
      application.id,
      language.id,
      DateTime.now,
      DateTime.now,
      rememberMe,
      timeout,
      ip,
      DateTime.now(),
      UUID.randomUUID())
    sessionDao.createSession(openSession)
  }

  def provideUserTenantData(requestor: ActorRef, msg: Login, exOpt: Option[AuthenticationException]): Unit =
    provideUserTenantData(
      // 1. Проверки пользователя:
      validateUser(msg.loginData.login, msg.loginData.password),
      requestor,
      exOpt)

  def provideUserTenantData(userFuture: Future[User], requestor: ActorRef, exOpt: Option[AuthenticationException]): Unit = {
    val future = for {
      user <- userFuture
      userTenantData <- tenantUserDao.getUserTenantData(user.id)
    } yield userTenantData
    future.foreach {
      case res =>
        //println("provideUserTenants: " + res)
        requestor ! AuthenticationService.UserTenantData(res, exOpt.map(_.exceptionMessage))
    }
    future.failed.foreach {
      case throwable: Throwable =>
        println("provideUserTenants: ")
        throwable.printStackTrace()
        requestor ! FailureResponse(new AuthenticationFailedException())
    }
  }

}

