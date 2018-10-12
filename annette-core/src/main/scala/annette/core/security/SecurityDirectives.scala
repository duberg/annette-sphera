package annette.core.security

import java.util.UUID

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.{ LogSource, Logging }
import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.model.{ HttpMethod, Uri }
import akka.http.scaladsl.server._
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import annette.core.security.authentication.{ AuthenticationService, Session }
import annette.core.security.authorization.AuthorizationActor.ValidateAuthorizedUser
import com.typesafe.config.Config
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Success, Try }

@Singleton
class SecurityDirectives @Inject() (
  @Named(AuthenticationService.name) val authenticationManager: ActorRef,
  @Named("AuthorizationManager") val authorizationManager: ActorRef,
  system: ActorSystem,
  config: Config) extends Directives {

  implicit val serviceTimeout: Timeout = 30.seconds // TODO: заменить на конфигурацию

  val debugMode = Try { config.getBoolean("annette.security.debug") }.toOption.getOrElse(false)
  val debugSessionId = Try { UUID.fromString(config.getString("annette.security.debugSession.session")) }.toOption
  val debugUserId = Try { UUID.fromString(config.getString("annette.security.debugSession.user")) }.toOption
  val debugTenantId = Try { config.getString("annette.security.debugSession.tenant") }.toOption
  val debugApplicationId = Try { config.getString("annette.security.debugSession.application") }.toOption
  val debugLanguageId = Try { config.getString("annette.security.debugSession.language") }.toOption

  implicit val myLogSourceType = new LogSource[SecurityDirectives] {
    def genString(a: SecurityDirectives) = "AnnetteSecurityDirectives"
  }

  val log = Logging(system, this)

  private def tokenValidation(maybeJwtToken: Option[String]): Directive[Tuple1[Option[Session]]] = {
    onComplete(getAndValidateSessionData(maybeJwtToken)).flatMap {
      case Success(sessionDataOpt) =>
        provide(sessionDataOpt)
      case _ =>
        provide(None)
    }
  }

  private def fakeTokenValidation(maybeJwtToken: Option[String]): Directive[Tuple1[Option[Session]]] = {
    log.warning("FakeTokenValidation has been used.")
    val (sessionId, userId, tenantId, applicationId, languageId) = maybeJwtToken.map {
      token =>
        val tokenElements = token.split(":")
        val sessionId = Try(UUID.fromString(tokenElements(0))).toOption.orElse(debugSessionId).getOrElse(UUID.randomUUID())
        val userId = Try(UUID.fromString(tokenElements(2))).toOption.orElse(debugUserId)
        val tenantId = Try(tokenElements(2)).toOption.flatMap(token => if (token.trim.isEmpty) debugTenantId else Some(token.trim))
        val applicationId = Try(tokenElements(3)).toOption.flatMap(token => if (token.trim.isEmpty) debugApplicationId else Some(token.trim))
        val languageId = Try(tokenElements(4)).toOption.flatMap(token => if (token.trim.isEmpty) debugLanguageId else Some(token.trim))
        (sessionId, userId, tenantId, applicationId, languageId)
    }.getOrElse((UUID.randomUUID(), debugUserId, debugTenantId, debugApplicationId, debugLanguageId))
    if (userId.isDefined && tenantId.isDefined && applicationId.isDefined && languageId.isDefined) {
      provide(Some(Session(
        sessionId = sessionId,
        userId = userId.get,
        tenantId = tenantId.get,
        applicationId = applicationId.get,
        languageId = languageId.get)))
    } else provide(None)
  }

  private val authReject = reject(
    AuthenticationFailedRejection.apply(
      AuthenticationFailedRejection.CredentialsMissing,
      HttpChallenge("Auth", Some("Annette"))))

  val authenticatedOpt: Directive1[Option[Session]] = optionalHeaderValueByName("Authorization")
    .flatMap {
      maybeJwtToken =>
        if (debugMode) fakeTokenValidation(maybeJwtToken)
        else tokenValidation(maybeJwtToken)
    }

  /**
   * = Authentication directive =
   */
  val authenticated: Directive1[Session] = authenticatedOpt.flatMap {
    case Some(session) => provide(session)
    case _ => authReject
  }

  private def getAndValidateSessionData(maybeJwtToken: Option[String]): Future[Option[Session]] = {
    maybeJwtToken
      .map {
        token =>
          authenticationManager
            .ask(AuthenticationService.Authenticate(token))
            .map {
              case AuthenticationService.Authenticated(sessionData) =>
                Some(sessionData)
              case _ =>
                None
            }
      }
      .getOrElse(FastFuture.successful(None))
  }

  def authorizationCheck(uri: Uri, httpMethod: HttpMethod, session: Session): Future[Boolean] =
    ask(authorizationManager, ValidateAuthorizedUser(
      userId = Some(session.userId.toString),
      accessPath = Some(uri.path.toString()),
      action = Some(httpMethod.value)))
      .mapTo[Boolean]

  /**
   * = Authorization directive =
   *
   * Для проверки прав доступа нужно извлечь uri ресурса, http метод и сессию пользователя.
   */
  val authorized: Directive1[Session] = {
    (extractUri & extractMethod & authenticated) tflatMap {
      case (uri, httpMethod, session) =>
        authorizeAsync(authorizationCheck(uri, httpMethod, session)) & provide(session)
    }
  }
}

