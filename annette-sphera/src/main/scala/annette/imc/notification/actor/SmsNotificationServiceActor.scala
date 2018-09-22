package annette.imc.notification.actor

import akka.actor.Props
import akka.util.Timeout
import annette.core.persistence.Persistence._
import annette.core.utils.Generator
import annette.imc.notification.SmsSettings
import annette.imc.notification.client.SmsClient
import annette.imc.notification.model.{ SmsNotification, _ }
import annette.imc.notification.template.SmsTemplates

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.util.{ Failure, Success, Try }

private class SmsNotificationServiceActor(
  val id: String,
  val retryInterval: FiniteDuration,
  val smsClient: SmsClient,
  val initState: SmsNotificationServiceState,
  implicit val t: Timeout = 30 seconds)(implicit val executor: ExecutionContext) extends PersistentStateActor[SmsNotificationServiceState]
  with Generator {
  import SmsNotificationServiceActor._

  type ClientResult = (SmsNotification, Try[SmsClient.Response])
  type ClientSuccess = (SmsNotification, SmsClient.Response)
  type ClientFailure = (SmsNotification, Throwable)

  def send: PartialFunction[SmsNotification, ClientResult] = {
    case x @ SmsNotification.Password(i, phone, password, language, retry) =>
      x -> smsClient.send(
        id = i,
        to = phone,
        message = SmsTemplates.password(language, password),
        from = SmsTemplates.subject(language))
    case x @ SmsNotification.Verification(i, phone, code, language, retry) =>
      x -> smsClient.send(
        id = i,
        to = phone,
        message = SmsTemplates.verification(language, code),
        from = SmsTemplates.subject(language))
    case x @ SmsNotification.ToExpertise(i, phone, language, retry) =>
      x -> smsClient.send(
        id = i,
        to = phone,
        message = SmsTemplates.toExpertise(language),
        from = SmsTemplates.subject(language))
    case x @ SmsNotification.ToReview(i, phone, language, retry) =>
      x -> smsClient.send(
        id = i,
        to = phone,
        message = SmsTemplates.toReview(language),
        from = SmsTemplates.subject(language))
  }

  def processResult: PartialFunction[ClientResult, ClientResult] = {
    case x @ (n, Success(_)) =>
      self ! DeleteNotificationCmd(n.id)
      x
    case x @ (n, Failure(e)) =>
      self ! UpdateRetryCmd(n.id, n.retry - 1)
      x
  }

  def hideCredentials: PartialFunction[(SmsNotification, Any), (SmsNotification, Any)] = {
    case (n: SmsNotification.Verification, x) if !smsClient.settings.debug =>
      (n.copy(code = hide(n.code)), x)
    case (n: SmsNotification.Password, x) if !smsClient.settings.debug =>
      (n.copy(password = hide(n.password)), x)
    case x => x
  }

  /**
   * Обработка ошибок.
   */
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
        case (n: SmsNotification.Password, e) => log.warning(s"Failed ${n.getClass.getSimpleName} notification [id: ${n.id}, phone:${n.phone}, password: ${n.password}] [$e]")
        case (n, e) => log.warning(s"Failed ${n.getClass.getSimpleName} notification [id: ${n.id}, phone:${n.phone}] [$e]")
      }
  }

  /**
   * Обработка результатов.
   *
   * В режиме отладки выводим код авторизации в консоль.
   */
  def processSuccess(success: Seq[ClientSuccess]): Unit =
    success
      .map(hideCredentials)
      .foreach {
        case (n: SmsNotification.Password, _) => log.info(s"${n.getClass.getSimpleName} notification [id: ${n.id}, phone:${n.phone}, password: ${n.password}]")
        case (n: SmsNotification.Verification, _) => log.info(s"${n.getClass.getSimpleName} verification [id: ${n.id}, phone:${n.phone}, code:${n.code}]")
        case (n, _) => log.info(s"${n.getClass.getSimpleName} notification [id: ${n.id}, phone:${n.phone}]")
      }

  def notify(state: SmsNotificationServiceState): Unit = {
    if (state.nonEmpty) {
      val results: Seq[ClientResult] =
        state.notifications.values.toSeq.map {
          send.andThen(processResult)
        }
      val success: Seq[ClientSuccess] = results.collect {
        case (n, Success(r: SmsClient.Response)) => (n, r)
      }
      val failures: Seq[ClientFailure] = results.collect {
        case (n, Failure(e: SmsClient.Exception)) => (n, e)
      }
      processFailures(failures)
      processSuccess(success)
    }
    notifyAfterRetry()
    if (sender != self) sender ! Done
  }

  def updateRetry(state: SmsNotificationServiceState, id: Notification.Id, retry: Int): Unit = {
    if (state.exists(id)) {
      persist(UpdatedRetryEvt(id, retry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotificationNotFound
  }

  def addNotification(state: SmsNotificationServiceState, x: SmsNotification): Unit = {
    if (state.exists(x.id)) sender ! NotificationAlreadyExists else {
      persist(AddedNotificationEvt(x)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }

  def deleteNotification(state: SmsNotificationServiceState, id: Notification.Id): Unit = {
    if (state.exists(id)) {
      persist(DeletedNotificationEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotificationNotFound
  }

  def findNotifications(state: SmsNotificationServiceState): Unit =
    sender() ! NotificationsRes(state.notifications)

  def behavior(state: SmsNotificationServiceState): Receive = {
    case AddNotificationCmd(x) => addNotification(state, x)
    case DeleteNotificationCmd(x) => deleteNotification(state, x)
    case GetNotifications => findNotifications(state)
    case Notify => notify(state)
    case UpdateRetryCmd(x, y) => updateRetry(state, x, y)
  }

  def notifyAfterRetry(): Unit =
    context.system.scheduler.scheduleOnce(retryInterval, self, Notify)

  override def afterRecover(state: SmsNotificationServiceState): Unit =
    context.system.scheduler.scheduleOnce(1 minute, self, Notify)
}

object SmsNotificationServiceActor {
  trait Command extends PersistentCommand
  trait Request extends PersistentQuery
  trait Response extends PersistentResponse
  trait Event extends PersistentEvent

  case class AddNotificationCmd(x: SmsNotification) extends Command
  case class DeleteNotificationCmd(id: Notification.Id) extends Command
  case class UpdateRetryCmd(id: Notification.Id, retry: Int) extends Command

  case object GetNotifications extends Request
  case object Notify extends Request

  case class AddedNotificationEvt(x: SmsNotification) extends PersistentEvent
  case class DeletedNotificationEvt(id: Notification.Id) extends PersistentEvent
  case class UpdatedRetryEvt(id: Notification.Id, retry: Int) extends PersistentEvent

  case object Done extends Response
  case object NotifyTimeoutException extends Response
  case object NotificationAlreadyExists extends Response
  case object NotificationNotFound extends Response
  case class NotificationsRes(x: Map[Notification.Id, SmsNotification]) extends Response

  def props(
    id: String,
    retryInterval: FiniteDuration,
    settings: SmsSettings,
    state: SmsNotificationServiceState = SmsNotificationServiceState.empty)(implicit c: ExecutionContext): Props =
    Props(new SmsNotificationServiceActor(id, retryInterval, new SmsClient(settings), state))

  def propsWithSmsClient(
    id: String,
    retryInterval: FiniteDuration,
    smsClient: SmsClient,
    state: SmsNotificationServiceState = SmsNotificationServiceState.empty)(implicit c: ExecutionContext): Props =
    Props(new SmsNotificationServiceActor(id, retryInterval, smsClient, state))
}
