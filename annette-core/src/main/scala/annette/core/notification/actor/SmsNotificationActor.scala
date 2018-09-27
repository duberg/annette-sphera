package annette.core.notification.actor

import akka.actor.Props
import akka.util.Timeout
import annette.core.notification.client.SmsClient
import annette.core.notification._
import annette.core.akkaext.actor._
import annette.core.akkaext.persistence._
import annette.core.utils.Generator

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.util.{ Failure, Success, Try }

private class SmsNotificationActor(
  val id: ActorId,
  val retryInterval: FiniteDuration,
  val smsClient: SmsClient,
  val initState: SmsNotificationState,
  implicit val t: Timeout = 30 seconds)(implicit val executor: ExecutionContext) extends CqrsPersistentActor[SmsNotificationState]
  with Generator {
  import SmsNotificationActor._

  type ClientResult = (SmsNotificationLike, Try[SmsClient.Response])
  type ClientSuccess = (SmsNotificationLike, SmsClient.Response)
  type ClientFailure = (SmsNotificationLike, Throwable)

  def send(x: SmsNotificationLike): ClientResult =
    x -> smsClient.send(
      id = x.id,
      to = x.phone,
      message = x.message,
      from = x.subject)

  def processResults(state: SmsNotificationState, x: Seq[ClientResult]): Unit =
    changeState((state /: x) {
      case (s, (n, Success(_))) => s.updated(DeletedNotificationEvt(n.id))
      case (s, (n, Failure(_))) => s.updated(UpdatedRetryEvt(n.id, n.retry - 1))
    })

  def hideCredentials: PartialFunction[(SmsNotificationLike, Any), (SmsNotificationLike, Any)] = {
    case (n: VerifyBySmsNotification, x) if !smsClient.settings.debug =>
      (n.copy(code = hide(n.code)), x)
    case (n: SendPasswordToPhoneNotification, x) if !smsClient.settings.debug =>
      (n.copy(password = hide(n.password)), x)
    case x => x
  }

  def processFailures(x: Seq[ClientFailure]): Unit = {
    val httpUnauthorizedException: Option[SmsClient.HttpUnauthorizedException] = x.map(_._2).collectFirst {
      case x: SmsClient.HttpUnauthorizedException => x
    }
    val otherExceptions: Seq[ClientFailure] = x.filter {
      case (n, x: SmsClient.HttpUnauthorizedException) => false
      case _ => true
    }
    httpUnauthorizedException.foreach { e =>
      log.error(s"Http Unauthorized Exception [$e]")
    }
    otherExceptions
      .map(hideCredentials)
      .foreach {
        case (n: SendPasswordToPhoneNotification, e) => log.warning(s"Failed ${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}, password: ${n.password}] [$e]")
        case (n, e) => log.warning(s"Failed ${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}] [$e]")
      }
  }

  def processSuccesses(success: Seq[ClientSuccess]): Unit =
    success
      .map(hideCredentials)
      .foreach {
        case (n: SendPasswordToPhoneNotification, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}, password: ${n.password}]")
        case (n: VerifyBySmsNotification, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}, code:${n.code}]")
        case (n, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}]")
      }

  def notify(state: SmsNotificationState): Unit = {
    if (state.nonEmpty) {
      val notifications = state.v.values.toSeq

      val results: Seq[ClientResult] = notifications.map(send)

      val success: Seq[ClientSuccess] = results.collect {
        case (n, Success(r: SmsClient.Response)) => (n, r)
      }

      val failures: Seq[ClientFailure] = results.collect {
        case (n, Failure(e: SmsClient.Exception)) => (n, e)
      }

      processResults(state, results)
      processFailures(failures)
      processSuccesses(success)
    }
    notifyAfterRetry()
    replyDone()
  }

  def createNotification(state: SmsNotificationState, x: CreateSmsNotificationLike): Unit = {
    val notification = x match {
      case y: CreatePasswordToPhoneNotification => SendPasswordToPhoneNotification(
        id = generateUUID,
        phone = y.phone,
        subject = y.subject,
        message = y.message,
        password = y.password)
      case y: CreateVerifyBySmsNotification => VerifyBySmsNotification(
        id = generateUUID,
        phone = y.phone,
        subject = y.subject,
        message = y.message,
        code = y.code)
      case y: CreateSmsNotificationLike => SmsNotification(
        id = generateUUID,
        phone = y.phone,
        subject = y.subject,
        message = y.message)
    }
    changeState(state.updated(CreatedNotificationEvt(notification)))
    sender ! CreateSmsNotificationSuccess(notification)
  }

  def listNotifications(state: SmsNotificationState): Unit =
    sender() ! NotificationMap(state.v)

  def behavior(state: SmsNotificationState): Receive = {
    case NotifyCmd => notify(state)
    case CreateNotificationCmd(x) => createNotification(state, x)
    case ListNotifications => listNotifications(state)
  }

  def notifyAfterRetry(): Unit =
    context.system.scheduler.scheduleOnce(
      delay = retryInterval,
      receiver = self,
      message = NotifyCmd)

  def replyDone(): Unit = if (sender != self) sender ! Done

  override def preStart(): Unit = {
    super.preStart()
    context.system.scheduler.scheduleOnce(
      delay = retryInterval,
      receiver = self,
      message = NotifyCmd)
  }
}

object SmsNotificationActor {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case object NotifyCmd extends Query
  case class CreateNotificationCmd(x: CreateSmsNotificationLike) extends Command

  case object ListNotifications extends Query

  case class CreatedNotificationEvt(x: SmsNotificationLike) extends Event
  case class DeletedNotificationEvt(notificationId: Notification.Id) extends Event
  case class UpdatedRetryEvt(notificationId: Notification.Id, retry: Int) extends Event

  case object Done extends Response
  case class CreateSmsNotificationSuccess(x: SmsNotificationLike)
  case object NotifyTimeoutException extends Response
  case object NotificationAlreadyExists extends Response
  case object NotificationNotFound extends Response
  case class NotificationMap(x: Map[Notification.Id, SmsNotificationLike]) extends Response

  def props(
    id: ActorId,
    retryInterval: FiniteDuration,
    settings: SmsSettings,
    state: SmsNotificationState = SmsNotificationState.empty)(implicit c: ExecutionContext): Props =
    Props(new SmsNotificationActor(id, retryInterval, new SmsClient(settings), state))

  def propsWithSmsClient(
    id: ActorId,
    retryInterval: FiniteDuration,
    smsClient: SmsClient,
    state: SmsNotificationState = SmsNotificationState.empty)(implicit c: ExecutionContext): Props =
    Props(new SmsNotificationActor(id, retryInterval, smsClient, state))
}
