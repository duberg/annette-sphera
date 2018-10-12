package annette.core.security.authentication

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.event.LoggingReceive
import akka.http.scaladsl.util.FastFuture
import akka.util.Timeout
import annette.core.domain.application.{ ApplicationManager, _ }
import annette.core.domain.language.LanguageManager
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.{ OpenSession, Tenant, TenantData, User }
import annette.core.domain.tenancy.{ SessionManager, TenantService, UserManager }
import annette.core.security.authentication.AuthenticationService.{ FailureResponse, Login }
import annette.core.security.authentication.jwt.JwtHelper
import org.joda.time.DateTime

import scala.concurrent.{ ExecutionContext, Future }

class LoginActor(
  userDao: UserManager,
  sessionDao: SessionManager,
  tenantService: TenantService,
  applicationDao: ApplicationManager,
  languageDao: LanguageManager,
  rememberMeSessionTimeout: Int,
  sessionTimeout: Int,
  val secret: String)(implicit c: ExecutionContext, t: Timeout) extends Actor with ActorLogging with JwtHelper {

  def validateUser(login: String, password: String): Future[User] = {
    for {
      userOpt <- userDao.getByLoginAndPassword(login, password)
    } yield {
      log.debug(s"validateUser: $userOpt")

      userOpt match {
        case Some(x) if x.status != 0 => x
        case Some(x) => throw new AccountDeactivatedException()
        case None => throw new AuthenticationFailedException()
      }
    }
  }

  override def receive: Receive = LoggingReceive {
    case x: AuthenticationService.Login =>
      if (x.credentials.selectTenant) provideUserTenantData(sender, x, None)
      else login(x)
  }

  /* ----------------------- Login ----------------------- */

  def login(msg: Login) = {
    val requestor = sender

    context.system.log.debug(msg.toString)

    val future = for {
      // 1. Проверки пользователя:
      user <- validateUser(msg.credentials.login, msg.credentials.password)

      userTenants <- tenantService.getUserTenantData(user.id)
      // получить организацию и приложение для входа
      (tenantId, applicationId) <- {
        val r = getTenantAndApplication(user.id, userTenants, msg.credentials.tenant, msg.credentials.application)
        r.foreach { case (tenantId, applicationId) => context.system.log.debug(s"getTenantAndApplication: tenantId: $tenantId, applicationId: $applicationId") }
        r.failed.foreach(th => context.system.log.debug(s"getTenantAndApplication: ${th.getMessage}"))
        r
      }
      // 4. Если все проверки пользователя пройдены, то при последующих ошибках проверки
      // пользователю предлагается выбрать организацию/приложение

      // 5. Проверки организации:
      tenant <- tenantService
        .getTenantById(tenantId)
        .map(_.getOrElse(throw new TenantNotFoundException()))

      // Проверки приложения в организации
      _ = if (!tenant.applications.contains(applicationId)) throw new ApplicationNotAssignedToTenantException()

      // Проверки языка в организации
      languageId = msg.credentials.language.getOrElse(tenant.defaultLanguageId)
      _ = if (!tenant.languages.contains(languageId)) throw new LanguageNotAssignedToTenantException()

      // 6. Проверки пользователя в организации:
      tenantUser <- tenantService.isUserAssignedToTenant(tenantId, user.id)
        .map(x => if (!x) throw new UserNotAssignedToTenantException())

      // 7. Проверки приложения:
      application <- applicationDao
        .getApplicationById(applicationId)
        .map(_.getOrElse(throw new ApplicationNotFoundException()))

      // Проверки языка:
      language <- languageDao
        .getLanguageById(languageId)
        .map(_.getOrElse(throw new LanguageNotFoundException()))

      languages <- languageDao.selectAll

      // 8. Если все проверки прошли создаём сессию:
      openSession <- createOpenSession(tenant, user, application, language, msg.credentials.rememberMe, msg.ip)
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
        throwable.printStackTrace()
        requestor ! FailureResponse(new AuthenticationFailedException())
    }
  }

  def getTenantAndApplication(
    userId: User.Id,
    tenantData: Set[TenantData],
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
          if (tenantData.size == 1 && tenantData.head.apps.size == 1) {
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
      LocalDateTime.now(),
      LocalDateTime.now(),
      rememberMe,
      timeout,
      ip,
      LocalDateTime.now(),
      UUID.randomUUID())
    sessionDao.createSession(openSession)
  }

  def provideUserTenantData(requestor: ActorRef, msg: Login, exOpt: Option[AuthenticationException]): Unit =
    provideUserTenantData(
      // 1. Проверки пользователя:
      validateUser(msg.credentials.login, msg.credentials.password),
      requestor,
      exOpt)

  def provideUserTenantData(userFuture: Future[User], requestor: ActorRef, exOpt: Option[AuthenticationException]): Unit = {
    val future = for {
      user <- userFuture
      userTenantData <- tenantService.getUserTenantData(user.id)
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

