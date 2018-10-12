package annette.core.security.authentication

import akka.actor.{ Actor, ActorLogging }
import akka.event.LoggingReceive
import annette.core.domain.application.ApplicationManager
import annette.core.domain.language.LanguageManager
import annette.core.domain.tenancy.model.OpenSession
import annette.core.domain.tenancy.{ SessionManager, TenantService, UserManager }
import annette.core.security.authentication.jwt.JwtHelper

import scala.concurrent.Future

class AuthenticationActor(
  sessionManager: SessionManager,
  tenantService: TenantService,
  applicationManager: ApplicationManager,
  userManager: UserManager,
  languageManager: LanguageManager,
  override val secret: String)
  extends Actor with ActorLogging with JwtHelper {

  implicit val ec = context.dispatcher

  override def receive: Receive = LoggingReceive {
    case AuthenticationService.Authenticate(jwtToken) =>
      authenticate(jwtToken)
  }

  private def authenticate(jwtToken: String) = {
    val requestor = sender
    decodeSessionData(jwtToken)
      .map {
        sessionData =>
          for {
            openSession <- validateSession(sessionData)
            //_ <- validateAssignments(openSession, sessionData) // TODO: а надо ли валидировать присвоения???
          } yield {
            AuthenticationService.Authenticated(sessionData)
          }
      }
      .fold(requestor ! AuthenticationService.AuthenticationFailed()) {
        future =>
          future.foreach { result => requestor ! result }
          future.failed.foreach { _ => requestor ! AuthenticationService.AuthenticationFailed() }
      }
  }
  private def validateSession(sessionData: Session): Future[OpenSession] = {
    for {
      openSessionOpt <- sessionManager.getOpenSessionById(sessionData.sessionId)
    } yield {
      openSessionOpt.fold(throw new AuthenticationFailedException()) {
        openSession =>
          if (sessionData.userId != openSession.userId) {
            throw new AuthenticationFailedException()
          }
          openSession
      }
    }
  }

  private def validateAssignments(session: OpenSession, sessionData: Session) = {
    val tenantId = sessionData.tenantId
    val applicationId = sessionData.applicationId
    for {
      userOpt <- userManager.getUserById(session.userId)
      tenantOpt <- tenantService.getTenantById(tenantId)
      applicationOpt <- applicationManager.getApplicationById(applicationId)
      tenantUserExist <- tenantService.isUserAssignedToTenant(tenantId, session.userId)
    } yield {
      if (userOpt.isEmpty) throw new UserNotFoundException()
      if (tenantOpt.isEmpty) throw new TenantNotFoundException()
      if (applicationOpt.isEmpty) throw new ApplicationNotFoundException()
      if (!tenantUserExist) throw new UserNotAssignedToTenantException()
      val tenant = tenantOpt.get
      if (!tenant.applications.contains(applicationId)) throw new ApplicationNotAssignedToTenantException
      (tenantId, applicationId)
    }
  }

}