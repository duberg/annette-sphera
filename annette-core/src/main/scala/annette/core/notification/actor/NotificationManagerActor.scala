package annette.core.notification.actor

import akka.actor.SupervisorStrategy.{ Escalate, Restart, Resume }
import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props }
import akka.util.Timeout
import annette.core.notification._
import annette.core.akkaext.actor.ActorLifecycleHooks
import com.typesafe.config.Config
import annette.core.security.verification.{ Verification, VerificationActor, VerificationBus }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class NotificationManagerActor(
  val id: NotificationManager.Id,
  val emailRetryInterval: FiniteDuration,
  val emailSettings: EmailSettings,
  val smsRetryInterval: FiniteDuration,
  val smsSettings: SmsSettings,
  val verificationBus: VerificationBus)(implicit c: ExecutionContext, t: Timeout) extends Actor
  with ActorLogging
  with ActorLifecycleHooks {
  import NotificationManagerActor._

  val emailNotificationActor: ActorRef = {
    context.actorOf(
      props = EmailNotificationActor.props(
        id = id / EmailNotificationActorName,
        retryInterval = emailRetryInterval,
        settings = emailSettings),
      name = EmailNotificationActorName)
  }

  val smsNotificationActor: ActorRef = {
    context.actorOf(
      props = SmsNotificationActor.props(
        id = id / SmsNotificationActorName,
        retryInterval = smsRetryInterval,
        settings = smsSettings),
      name = SmsNotificationActorName)
  }

  val verificationActor: ActorRef = {
    context.actorOf(
      props = VerificationActor.props(id = id / VerificationActorName, bus = verificationBus),
      name = VerificationActorName)
  }

  val webSocketNotificationActor: ActorRef = {
    context.actorOf(
      props = WebSocketNotificationActor.props(
        id = id / WebSocketNotificationActorName),
      name = WebSocketNotificationActorName)
  }

  override val supervisorStrategy: OneForOneStrategy = {
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case e: ArithmeticException =>
        log.error(e, e.getMessage)
        Resume
      case e: NullPointerException =>
        log.error(e, e.getMessage)
        Restart
      case e: IllegalArgumentException =>
        log.debug(e.getMessage)
        Resume
      case e: Exception =>
        log.error(e, e.getMessage)
        Resume
      case e =>
        super.supervisorStrategy.decider.applyOrElse(e, (_: Any) => Escalate)
    }
  }

  def receive: Receive = {
    case x: EmailNotificationActor.Command => emailNotificationActor forward x
    case x: SmsNotificationActor.Command => smsNotificationActor forward x
    case x: Verification.Command => verificationActor forward x
    case x: WebSocketNotificationActor.Command => webSocketNotificationActor forward x

    case x: EmailNotificationActor.Query => emailNotificationActor forward x
    case x: SmsNotificationActor.Query => smsNotificationActor forward x
    case x: Verification.Query => verificationActor forward x
    case x: WebSocketNotificationActor.Query => webSocketNotificationActor forward x
  }
}

object NotificationManagerActor extends NotificationConfig {
  val EmailNotificationActorName = "email"
  val SmsNotificationActorName = "sms"
  val VerificationActorName = "smsVerification"
  val WebSocketNotificationActorName = "ws"

  def props(
    id: NotificationManager.Id,
    config: Config,
    verificationBus: VerificationBus)(implicit c: ExecutionContext, t: Timeout): Props = {
    val annetteConfig: Config = config.getConfig("annette")
    val mailNotificationConfig = annetteConfig.emailNotificationEntry
    val smsNotificationConfig = annetteConfig.smsNotificationEntry
    Props(new NotificationManagerActor(
      id = id,
      emailRetryInterval = mailNotificationConfig.retryInterval,
      emailSettings = mailNotificationConfig.email,
      smsRetryInterval = smsNotificationConfig.retryInterval,
      smsSettings = smsNotificationConfig.sms,
      verificationBus = verificationBus))
  }
}