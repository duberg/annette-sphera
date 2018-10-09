package annette.core.domain

import javax.inject.{ Named, Singleton }
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.util.Timeout
import annette.core.akkaext.actor.ActorId
import annette.core.domain.application._
import annette.core.domain.language.LanguageService
import annette.core.domain.tenancy.{ LastSessionManager, OpenSessionManager, SessionHistoryManager, UserManager }
import annette.core.notification.actor.NotificationManagerActor
import com.typesafe.config.Config
import annette.core.domain.tenancy.model.{ Tenant, User }

import scala.concurrent.ExecutionContext
import annette.core.domain.CoreService._
import annette.core.domain.tenancy.actor.TenantManagerActor
import annette.core.notification.actor._
import annette.core.security.verification.{ Verification, VerificationBus }

@Singleton
@Named("CoreService")
class CoreManagerActor(config: Config, verificationBus: VerificationBus)(implicit c: ExecutionContext, t: Timeout) extends Actor with ActorLogging {
  val coreId = ActorId("core")

  val tenantManagerId = coreId / "tenant"
  val tenantManagerActor = context.actorOf(
    props = TenantManagerActor.props(tenantManagerId),
    name = "tenant")

  val applicationManagerId = coreId / "application"
  val applicationActor: ActorRef = context.actorOf(ApplicationManager.props(applicationManagerId), "application")

  val languageActorId = coreId / "language"
  val languageActor: ActorRef = context.actorOf(LanguageService.props(languageActorId), "language")

  val userActorId = coreId / "user"
  val userActor: ActorRef = context.actorOf(UserManager.props(id = userActorId, verificationBus = verificationBus), "user")

  val lastSessionActorId = coreId / "last-session"
  val lastSessionActor: ActorRef = context.actorOf(LastSessionManager.props(lastSessionActorId), "last-session")

  val sessionHistoryActorId = coreId / "session-history"
  val sessionHistoryActor: ActorRef = context.actorOf(SessionHistoryManager.props(sessionHistoryActorId), "session-history")

  val openSessionActorId = coreId / "core-open-session"
  val openSessionActor: ActorRef = context.actorOf(OpenSessionManager.props(openSessionActorId, lastSessionActor, sessionHistoryActor), "open-session")

  val notificationManagerId = coreId / NotificationManagerActorName
  val notificationManagerActor: ActorRef = context.actorOf(
    props = NotificationManagerActor.props(
      id = notificationManagerId,
      config,
      verificationBus = verificationBus),
    name = NotificationManagerActorName)

  def receive: PartialFunction[Any, Unit] = {
    case x: Tenant.Command => tenantManagerActor forward x
    case x: Tenant.Query => tenantManagerActor forward x

    case x: Application.Command => applicationActor forward x
    case x: Application.Query => applicationActor forward x

    case x: LanguageService.Command => languageActor forward x
    case x: LanguageService.Query => languageActor forward x

    case x: User.Command => userActor forward x
    case x: User.Query => userActor forward x

    case x: OpenSessionManager.Command => openSessionActor forward x
    case x: OpenSessionManager.Query => openSessionActor forward x

    case x: LastSessionManager.Command => lastSessionActor forward x
    case x: LastSessionManager.Query => lastSessionActor forward x

    case x: SessionHistoryManager.Command => sessionHistoryActor forward x
    case x: SessionHistoryManager.Query => sessionHistoryActor forward x

    case x: EmailNotificationActor.Command => notificationManagerActor forward x
    case x: SmsNotificationActor.Command => notificationManagerActor forward x
    case x: Verification.Command => notificationManagerActor forward x
    case x: WebSocketNotificationActor.Command => notificationManagerActor forward x

    case x: EmailNotificationActor.Query => notificationManagerActor forward x
    case x: SmsNotificationActor.Query => notificationManagerActor forward x
    case x: Verification.Query => notificationManagerActor forward x
    case x: WebSocketNotificationActor.Query => notificationManagerActor forward x
  }
}

object CoreService {
  val NotificationManagerActorName = "notification"

  val name = "core"

  def props(config: Config, verificationBus: VerificationBus)(implicit c: ExecutionContext, t: Timeout) =
    Props(new CoreManagerActor(config = config, verificationBus = verificationBus))
}
