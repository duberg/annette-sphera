package annette.core.domain

import javax.inject.{ Named, Singleton }
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.util.Timeout
import annette.core.akkaext.actor.ActorId
import annette.core.domain.application._
import annette.core.domain.language.LanguageService
import annette.core.domain.tenancy.{ LastSessionService, OpenSessionService, SessionHistoryService, UserManager }
import annette.core.notification.actor.NotificationManagerActor
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import annette.core.domain.CoreService._
import annette.core.notification.actor._
import annette.core.security.verification.Verification

@Singleton
@Named("CoreService")
class CoreServiceActor(config: Config)(implicit c: ExecutionContext, t: Timeout) extends Actor with ActorLogging {
  val applicationActor: ActorRef = context.actorOf(ApplicationManager.props("core-application"), "application")
  val languageActor: ActorRef = context.actorOf(LanguageService.props("core-language"), "language")
  val userActor: ActorRef = context.actorOf(UserManager.props("core-user"), "user")

  val lastSessionActor: ActorRef = context.actorOf(LastSessionService.props("core-last-session"), "last-session")
  val sessionHistoryActor: ActorRef = context.actorOf(SessionHistoryService.props("core-session-history"), "session-history")
  val openSessionActor: ActorRef = context.actorOf(OpenSessionService.props("core-open-session", lastSessionActor, sessionHistoryActor), "open-session")

  val coreId = ActorId("core")
  val notificationManagerId = coreId / NotificationManagerActorName
  val notificationManagerActor: ActorRef = context.actorOf(
    props = NotificationManagerActor.props(notificationManagerId, config),
    name = NotificationManagerActorName)

  def receive: PartialFunction[Any, Unit] = {
    case msg: Application.Command =>
      applicationActor forward msg
    case msg: Application.Query =>
      applicationActor forward msg
    case msg: LanguageService.Command =>
      languageActor forward msg
    case msg: LanguageService.Query =>
      languageActor forward msg
    case msg: UserManager.Command =>
      userActor forward msg
    case msg: UserManager.Query =>
      userActor forward msg
    case msg: OpenSessionService.Command =>
      openSessionActor forward msg
    case msg: OpenSessionService.Query =>
      openSessionActor forward msg
    case msg: LastSessionService.Command =>
      lastSessionActor forward msg
    case msg: LastSessionService.Query =>
      lastSessionActor forward msg
    case msg: SessionHistoryService.Command =>
      sessionHistoryActor forward msg
    case msg: SessionHistoryService.Query =>
      sessionHistoryActor forward msg

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

  def props(config: Config)(implicit c: ExecutionContext, t: Timeout) = Props(new CoreServiceActor(config))
}
