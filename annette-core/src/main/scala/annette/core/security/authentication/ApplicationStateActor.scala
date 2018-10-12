package annette.core.security.authentication

import akka.actor.{ Actor, ActorLogging }
import akka.event.LoggingReceive
import akka.pattern.pipe
import annette.core.domain.application.{ ApplicationService, _ }
import annette.core.domain.language.LanguageService
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.Tenant
import annette.core.domain.tenancy.{ SessionService, TenantService, UserService }
import annette.core.security.authentication.jwt.JwtHelper

import scala.concurrent.Future

class ApplicationStateActor(
  sessionManager: SessionService,
  TenantService: TenantService,
  applicationManager: ApplicationService,
  userManager: UserService,
  languageManager: LanguageService,
  override val secret: String)
  extends Actor with ActorLogging with JwtHelper {

  implicit val ec = context.dispatcher

  override def receive: Receive = LoggingReceive {

    case AuthenticationService.GetApplicationState(maybeSessionData) =>
      getApplicationState(maybeSessionData) pipeTo sender

    case AuthenticationService.SetApplicationState(sessionData, tenantId, applicationId, languageId) =>
      setApplicationState(sessionData, tenantId, applicationId, languageId) pipeTo sender
  }

  def getApplicationState(maybeSessionData: Option[Session]): Future[ApplicationState] = {
    maybeSessionData
      .map {
        sessionData =>
          for {
            userOpt <- userManager.getUserById(sessionData.userId)
            tenantOpt <- TenantService.getTenantById(sessionData.tenantId)
            applicationOpt <- applicationManager.getApplicationById(sessionData.applicationId)
            languages <- languageManager.selectAll
            userTenantData <- TenantService.getUserTenantData(sessionData.userId)
          } yield {
            val language = languages.find(_.id == sessionData.languageId).getOrElse(languages.head)
            val jwtToken = encodeSessionData(sessionData)
            ApplicationState(
              authenticated = true,
              language = language,
              languages = languages,
              user = userOpt,
              tenant = tenantOpt,
              application = applicationOpt,
              tenantData = userTenantData,
              jwtToken = Some(jwtToken))
          }
      }
      .getOrElse {
        for {
          languages <- languageManager.selectAll
        } yield {
          val language = languages.find(_.id == "RU").getOrElse(languages.head) // TODO: заменить константу на настройку
          ApplicationState(
            authenticated = false,
            language = language,
            languages = languages.toSeq)
        }
      }
  }

  def setApplicationState(
    sessionData: Session,
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id): Future[ApplicationState] = {
    for {
      userOpt <- userManager.getUserById(sessionData.userId)
      tenantOpt <- TenantService.getTenantById(tenantId)
      applicationOpt <- applicationManager.getApplicationById(applicationId)
      languages <- languageManager.selectAll
      userTenantData <- TenantService.getUserTenantData(sessionData.userId)
    } yield {
      val languageOpt = languages.find(_.id == languageId)

      if (tenantOpt.isEmpty) throw new TenantNotFoundException
      val tenantDataOpt = userTenantData.find(td => td.id == tenantId)
      if (tenantDataOpt.isEmpty) throw new UserNotAssignedToTenantException
      if (applicationOpt.isEmpty) throw new ApplicationNotFoundException
      if (!tenantOpt.get.applications.contains(applicationId)) throw new ApplicationNotAssignedToTenantException
      if (languageOpt.isEmpty) throw new LanguageNotFoundException
      if (!tenantOpt.get.languages.contains(languageId)) throw new LanguageNotAssignedToTenantException

      val jwtToken = encodeSessionData(sessionData.copy(tenantId = tenantId, applicationId = applicationId, languageId = languageId))

      sessionManager.updateTenantApplicationLanguage(sessionData.sessionId, tenantId, applicationId, languageId)

      ApplicationState(
        authenticated = true,
        language = languageOpt.get,
        languages = languages,
        user = userOpt,
        tenant = tenantOpt,
        application = applicationOpt,
        tenantData = userTenantData,
        jwtToken = Some(jwtToken))
    }

  }

}