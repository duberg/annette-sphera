package annette.core.domain

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.util.Timeout
import annette.core.domain.application._
import annette.core.domain.language.LanguageService
import annette.core.domain.tenancy.actor.TenantServiceActor
import annette.core.domain.tenancy.model.{ Tenant, User }
import annette.core.domain.tenancy.{ LastSessionManager, OpenSessionManager, SessionHistoryManager, UserService }
import annette.core.notification.actor.{ NotificationManagerActor, _ }
import annette.core.security.verification.{ Verification, VerificationBus }
import com.typesafe.config.Config
import javax.inject.{ Named, Singleton }

import scala.concurrent.ExecutionContext

@Singleton
@Named("CoreService")
class CoreServiceActor(config: Config, verificationBus: VerificationBus)(implicit c: ExecutionContext, t: Timeout) extends Actor with ActorLogging {
  val tenantServiceActor: ActorRef = context.actorOf(props = TenantServiceActor.props, name = "tenant")
  val applicationActor: ActorRef = context.actorOf(props = ApplicationService.props, name = "application")
  val languageActor: ActorRef = context.actorOf(props = LanguageService.props, name = "language")
  val userActor: ActorRef = context.actorOf(props = UserService.props(verificationBus = verificationBus), name = "user")
  val lastSessionActor: ActorRef = context.actorOf(props = LastSessionManager.props, name = "last-session")
  val sessionHistoryActor: ActorRef = context.actorOf(props = SessionHistoryManager.props, name = "session-history")
  val openSessionActor: ActorRef = context.actorOf(props = OpenSessionManager.props(lastSessionActor, sessionHistoryActor), name = "open-session")
  val notificationServiceActor: ActorRef = context.actorOf(
    props = NotificationManagerActor.props(config, verificationBus = verificationBus),
    name = "notification")

  def receive: PartialFunction[Any, Unit] = {
    case x: Tenant.Command => tenantServiceActor forward x
    case x: Tenant.Query => tenantServiceActor forward x

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

    case x: EmailNotificationActor.Command => notificationServiceActor forward x
    case x: SmsNotificationActor.Command => notificationServiceActor forward x
    case x: Verification.Command => notificationServiceActor forward x
    case x: WebSocketNotificationActor.Command => notificationServiceActor forward x

    case x: EmailNotificationActor.Query => notificationServiceActor forward x
    case x: SmsNotificationActor.Query => notificationServiceActor forward x
    case x: Verification.Query => notificationServiceActor forward x
    case x: WebSocketNotificationActor.Query => notificationServiceActor forward x
  }
}

object CoreService {
  val name = "core"

  def props(config: Config, verificationBus: VerificationBus)(implicit c: ExecutionContext, t: Timeout) =
    Props(new CoreServiceActor(config = config, verificationBus = verificationBus))
}
