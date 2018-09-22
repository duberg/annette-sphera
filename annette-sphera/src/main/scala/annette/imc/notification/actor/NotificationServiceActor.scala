package annette.imc.notification.actor

import akka.actor.SupervisorStrategy.{ Escalate, Restart, Resume }
import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props }
import akka.util.Timeout
import annette.core.utils.ActorLifecycleHooks
import annette.imc.notification._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

private class NotificationServiceActor(
  id: NotificationService.Id,
  apsActor: ActorRef,
  mailRetryInterval: FiniteDuration,
  mailSettings: MailSettings,
  smsRetryInterval: FiniteDuration,
  smsSettings: SmsSettings)(implicit executor: ExecutionContext, timeout: Timeout) extends Actor with ActorLogging with ActorLifecycleHooks {
  import NotificationServiceActor._

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

  val mailNotificationService: ActorRef = {
    context.actorOf(
      MailNotificationServiceActor.props(
        id = s"$id-$MailNotificationServiceName",
        retryInterval = mailRetryInterval,
        settings = mailSettings),
      MailNotificationServiceName)
  }
  val smsNotificationService: ActorRef = {
    context.actorOf(
      SmsNotificationServiceActor.props(
        id = s"$id-$SmsNotificationServiceName",
        retryInterval = smsRetryInterval,
        settings = smsSettings),
      SmsNotificationServiceName)
  }
  val smsVerificationService: ActorRef = {
    context.actorOf(
      SmsVerificationServiceActor.props(
        id = s"$id-$SmsVerificationServiceName",
        apsActor = apsActor,
        smsNotificationServiceActor = smsNotificationService),
      SmsVerificationServiceName)
  }

  def receive: Receive = {
    case x: MailNotificationServiceActor.Command => mailNotificationService forward x
    case x: SmsNotificationServiceActor.Command => smsNotificationService forward x
    case x: SmsVerificationServiceActor.Command => smsVerificationService forward x

    case x: MailNotificationServiceActor.Request => mailNotificationService forward x
    case x: SmsNotificationServiceActor.Request => smsNotificationService forward x
    case x: SmsVerificationServiceActor.Request => smsVerificationService forward x
  }
}

object NotificationServiceActor extends NotificationConfig {
  val MailNotificationServiceName = "mail"
  val SmsNotificationServiceName = "sms"
  val SmsVerificationServiceName = "sms-verification"

  def props(
    id: NotificationService.Id,
    apsActor: ActorRef,
    config: Config)(implicit c: ExecutionContext, t: Timeout): Props = {
    val annetteConfig: Config = config.getConfig("annette")
    val mailNotificationConfig = annetteConfig.mailNotificationEntry
    val smsNotificationConfig = annetteConfig.smsNotificationEntry
    Props(new NotificationServiceActor(
      id = id,
      apsActor = apsActor,
      mailRetryInterval = mailNotificationConfig.retryInterval,
      mailSettings = mailNotificationConfig.mail,
      smsRetryInterval = smsNotificationConfig.retryInterval,
      smsSettings = smsNotificationConfig.sms))
  }
}
