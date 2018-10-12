package annette.core.security.authentication

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ Actor, ActorLogging, Props }
import akka.event.LoggingReceive
import akka.routing.FromConfig
import akka.util.Timeout
import annette.core.domain.application.{ ApplicationService, _ }
import annette.core.domain.language.LanguageService
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.{ Tenant, TenantData }
import annette.core.domain.tenancy.{ SessionService, TenantService, UserService }
import annette.core.security.authentication.AuthenticationService._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.util.Try

class AuthenticationService(
  sessionManager: SessionService,
  TenantService: TenantService,
  applicationManager: ApplicationService,
  userManager: UserService,
  languageManager: LanguageService,
  config: Config)(implicit c: ExecutionContext) extends Actor with ActorLogging {
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
    props = FromConfig.props(
      Props(
        new LoginActor(
          userDao = userManager,
          sessionDao = sessionManager,
          tenantService = TenantService,
          applicationDao = applicationManager,
          languageDao = languageManager,
          rememberMeSessionTimeout = rememberMeSessionTimeout,
          sessionTimeout = sessionTimeout,
          secret = secret))),
    name = "login")

  val logoutService = context.actorOf(
    props = FromConfig.props(Props(new LogoutActor(sessionManager))),
    name = "logout")

  val authenticateService = context.actorOf(
    FromConfig.props(
      Props(classOf[AuthenticationActor], sessionManager, TenantService, applicationManager, userManager, languageManager, secret)),
    "authenticate")

  val applicationStateService = context.actorOf(
    FromConfig.props(
      Props(classOf[ApplicationStateActor], sessionManager, TenantService, applicationManager, userManager, languageManager, secret)),
    "applicationState")

  def receive: Receive = LoggingReceive {
    case x: Authenticate => authenticateService forward x
    case x: GetApplicationState => applicationStateService forward x
    case x: SetApplicationState => applicationStateService forward x
    case x: Login => loginService forward x
    case x: Logout => logoutService forward x
    case x: UpdateLastOpTimestamp => sessionManager.updateLastOpTimestamp(x.sessionId)
  }
}

object AuthenticationService {
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
    userTenantData: Set[TenantData],
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
    sessionManager: SessionService,
    TenantService: TenantService,
    applicationManager: ApplicationService,
    userManager: UserService,
    languageManager: LanguageService,
    config: Config)(implicit c: ExecutionContext): Props = {
    Props(
      new AuthenticationService(
        sessionManager = sessionManager,
        TenantService = TenantService,
        applicationManager = applicationManager,
        userManager = userManager,
        languageManager = languageManager,
        config = config))
  }

}
