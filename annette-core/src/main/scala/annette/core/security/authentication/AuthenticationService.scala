package annette.core.security.authentication

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ Actor, ActorLogging, Props }
import akka.event.LoggingReceive
import akka.routing.FromConfig
import akka.util.Timeout
import annette.core.domain.application.ApplicationManager
import annette.core.domain.application._
import annette.core.domain.language.LanguageManager
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.UserManager
import annette.core.domain.tenancy.dao._
import annette.core.domain.tenancy.model.Tenant
import com.typesafe.config.Config

import scala.util.Try

object AuthenticationService {

  /**
   * Наименование актора сервиса аутентификации
   */
  final val name = "AuthenticationService"

  class Message
  class Response
  case class FailureResponse(throwable: Throwable) extends Response

  /**
   * Запрос аутентификации по переданным реквизитам
   *
   * @param ip IP адрес пользователя
   */
  case class Login(credentials: Credentials, ip: String) extends Message

  case class Credentials(
    login: String, // логин пользователя: телефон или email
    password: String, // пароль пользователя
    rememberMe: Boolean, // если индикатор rememberMe = false, то создаётся сессионный cookie, иначе постоянный
    selectTenant: Boolean,
    language: Option[Language.Id], // желаемый язык входа
    tenant: Option[Tenant.Id],
    application: Option[Application.Id])

  case class UserTenantData(
    userTenantData: Seq[TenantData],
    messageCode: Option[Map[String, String]]) extends Response

  case class Logout(token: UUID) extends Message
  case class LoggedOut(token: UUID) extends Response

  /**
   * Запрос проверки аутентификаци по токену в сессии пользоватея
   *
   */
  case class Authenticate(jwtToken: String) extends Message

  case class Authenticated(sessionData: Session) extends Response
  case class AuthenticationFailed() extends Response

  case class UpdateLastOpTimestamp(
    sessionId: UUID) extends Message

  case class GetApplicationState(maybeSessionData: Option[Session]) extends Message

  case class SetApplicationState(
    sessionData: Session,
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id)

  def props(
    sessionDao: SessionDao,
    tenantDao: TenantDao,
    applicationDao: ApplicationManager,
    userDao: UserManager,
    tenantUserDao: TenantUserDao,
    languageDao: LanguageManager,
    config: Config) = Props(
    classOf[AuthenticationService],
    sessionDao,
    tenantDao,
    applicationDao,
    userDao,
    tenantUserDao,
    languageDao,
    config)

}

class AuthenticationService(
  sessionDao: SessionDao,
  tenantDao: TenantDao,
  applicationDao: ApplicationManager,
  userDao: UserManager,
  tenantUserDao: TenantUserDao,
  languageDao: LanguageManager,
  config: Config)
  extends Actor with ActorLogging {

  implicit val ec = context.dispatcher

  val secret = config.getString("annette.secret")

  implicit val timeout = Timeout(
    Try { config.getDuration("annette.core.AuthenticationService.timeout", TimeUnit.MILLISECONDS) }
      .getOrElse(10000),
    TimeUnit.MILLISECONDS)

  val rememberMeSessionTimeout = config
    .getDuration("annette.core.AuthenticationService.rememberMeSessionTimeout", TimeUnit.MINUTES)
    .toInt

  val sessionTimeout = config
    .getDuration("annette.core.AuthenticationService.sessionTimeout", TimeUnit.MINUTES)
    .toInt

  val loginService = context.actorOf(
    FromConfig.props(
      Props(
        classOf[LoginActor],
        userDao, sessionDao, tenantDao, tenantUserDao, applicationDao, languageDao,
        rememberMeSessionTimeout, sessionTimeout, secret)),
    "login")

  val logoutService = context.actorOf(
    FromConfig.props(
      Props(classOf[LogoutActor], sessionDao)),
    "logout")

  val authenticateService = context.actorOf(
    FromConfig.props(
      Props(classOf[AuthenticationActor], sessionDao, tenantDao, applicationDao, userDao, tenantUserDao, languageDao, secret)),
    "authenticate")

  val applicationStateService = context.actorOf(
    FromConfig.props(
      Props(classOf[ApplicationStateActor], sessionDao, tenantDao, applicationDao, userDao, tenantUserDao, languageDao, secret)),
    "applicationState")

  override def receive: Receive = LoggingReceive {
    case msg: AuthenticationService.Authenticate =>
      authenticateService forward msg

    case msg: AuthenticationService.GetApplicationState =>
      applicationStateService forward msg

    case msg: AuthenticationService.SetApplicationState =>
      applicationStateService forward msg

    case msg: AuthenticationService.Login =>
      loginService forward msg

    case msg: AuthenticationService.Logout =>
      logoutService forward msg

    case msg: AuthenticationService.UpdateLastOpTimestamp =>
      sessionDao.updateLastOpTimestamp(msg.sessionId)

  }
}
