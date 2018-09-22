package annette.imc.notification.actor

import javax.mail.AuthenticationFailedException

import akka.actor.Props
import annette.core.persistence.Persistence._
import annette.core.utils.Generator
import annette.imc.notification.MailSettings
import annette.imc.notification.client.MailClient
import annette.imc.notification.model._
import annette.imc.notification.template.MailTemplates

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.util.{ Failure, Success, Try }

private class MailNotificationServiceActor(
  val id: String,
  val retryInterval: FiniteDuration,
  val mailClient: MailClient,
  val initState: MailNotificationServiceState)(implicit val executor: ExecutionContext) extends PersistentStateActor[MailNotificationServiceState]
  with Generator {
  import MailNotificationServiceActor._

  type ClientResult = (MailNotification, Try[MailClient.Response])
  type ClientSuccess = (MailNotification, MailClient.Response)
  type ClientFailure = (MailNotification, Throwable)

  def send: PartialFunction[MailNotification, ClientResult] = {
    case x: MailNotification.Password =>
      x -> mailClient.send(
        to = x.email,
        subject = MailTemplates.subject(x.language),
        message = MailTemplates.password(x.language, x.password, x.templateParameters))
    case x: MailNotification.ToExpertise =>
      x -> mailClient.send(
        to = x.email,
        subject = MailTemplates.subject(x.language),
        message = MailTemplates.toExpertise(x.language, x.templateParameters))
    case x: MailNotification.ToReview =>
      x -> mailClient.send(
        to = x.email,
        subject = MailTemplates.subject(x.language),
        message = MailTemplates.toReview(x.language, x.templateParameters))
    case x: MailNotification.NotReady =>
      x -> mailClient.send(
        to = x.email,
        subject = MailTemplates.subject("RU"),
        message = MailTemplates.notReady("RU", x.templateParameters))
  }

  def processResult: PartialFunction[ClientResult, ClientResult] = {
    case x @ (n, Success(_)) =>
      self ! DeleteNotificationCmd(n.id)
      x
    case x @ (n, Failure(e)) =>
      self ! UpdateRetryCmd(n.id, n.retry - 1)
      x
  }

  def hideCredentials: PartialFunction[(MailNotification, Any), (MailNotification, Any)] = {
    case (n: MailNotification.Password, x) if !mailClient.settings.debug =>
      (n.copy(password = hide(n.password)), x)
    case x => x
  }

  /**
   * Обработка ошибок.
   */
  def processFailures(failures: Seq[ClientFailure]): Unit = {
    failures
      .map(hideCredentials)
      .foreach {
        case (n: MailNotification.Password, e) => log.warning(s"Failed ${n.getClass.getSimpleName} notification [id: ${n.id}, email:${n.email}, password: ${n.password}] [$e]")
        case (n, e) => log.warning(s"Failed ${n.getClass.getSimpleName} notification [id: ${n.id}, email:${n.email}] [$e]")
      }
  }

  /**
   * Обработка результатов.
   */
  def processSuccess(success: Seq[ClientSuccess]): Unit = {
    success
      .map(hideCredentials)
      .foreach {
        case (n: MailNotification.Password, _) => log.info(s"${n.getClass.getSimpleName} notification [id: ${n.id}, email:${n.email}, password: ${n.password}]")
        case (n, _) => log.info(s"${n.getClass.getSimpleName} notification [id: ${n.id}, email:${n.email}]")
      }
  }

  def notify(state: MailNotificationServiceState): Unit = {
    if (state.nonEmpty) {
      mailClient.connect() match {
        case Success(_) =>
          val results: Seq[ClientResult] =
            state.notifications.values.toSeq.map {
              send.andThen(processResult)
            }
          val success: Seq[ClientSuccess] = results.collect {
            case (n, Success(r: MailClient.Response)) => (n, r)
          }
          val failures: Seq[ClientFailure] = results.collect {
            case (n, Failure(e: MailClient.Exception)) => (n, e)
          }
          processFailures(failures)
          processSuccess(success)
          mailClient.disconnect()
        case Failure(e) =>
          state.notifications.values.foreach(n => self ! UpdateRetryCmd(n.id, n.retry - 1))
          e match {
            case x: AuthenticationFailedException => log.error(s"Failed credentials [$e]")
            case _ => log.error(s"Failed creating new smtp connection [$e]")
          }
      }
    }
    notifyAfterRetry()
    if (sender != self) sender ! Done
  }

  def updateRetry(state: MailNotificationServiceState, id: Notification.Id, retry: Int): Unit = {
    if (state.exists(id)) {
      persist(UpdatedRetryEvt(id, retry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotificationNotFound
  }

  def addNotification(state: MailNotificationServiceState, x: MailNotification): Unit = {
    if (state.exists(x.id)) sender ! NotificationAlreadyExists else {
      persist(AddedNotificationEvt(x)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }

  def deleteNotification(state: MailNotificationServiceState, id: Notification.Id): Unit = {
    if (state.exists(id)) {
      persist(DeletedNotificationEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotificationNotFound
  }

  def findNotifications(state: MailNotificationServiceState): Unit =
    sender() ! NotificationsRes(state.notifications)

  def behavior(state: MailNotificationServiceState): Receive = {
    case AddNotificationCmd(x) => addNotification(state, x)
    case DeleteNotificationCmd(x) => deleteNotification(state, x)
    case GetNotifications => findNotifications(state)
    case Notify => notify(state)
    case UpdateRetryCmd(x, y) => updateRetry(state, x, y)
  }

  def notifyAfterRetry(): Unit =
    context.system.scheduler.scheduleOnce(retryInterval, self, Notify)

  override def afterRecover(state: MailNotificationServiceState): Unit =
    context.system.scheduler.scheduleOnce(1 minute, self, Notify)
}

object MailNotificationServiceActor {
  trait Command extends PersistentCommand
  trait Request extends PersistentQuery
  trait Response extends PersistentResponse
  trait Event extends PersistentEvent

  case class AddNotificationCmd(x: MailNotification) extends Command
  case class DeleteNotificationCmd(id: Notification.Id) extends Command
  case class UpdateRetryCmd(id: Notification.Id, retry: Int) extends Command

  case object GetNotifications extends Request
  case object Notify extends Request

  case class AddedNotificationEvt(x: MailNotification) extends Event
  case class DeletedNotificationEvt(id: Notification.Id) extends Event
  case class UpdatedRetryEvt(id: Notification.Id, retry: Int) extends Event

  case object Done extends Response
  case object NotificationAlreadyExists extends Response
  case object NotificationNotFound extends Response
  case class NotificationsRes(x: Map[Notification.Id, MailNotification]) extends Response

  def props(
    id: String,
    retryInterval: FiniteDuration,
    settings: MailSettings,
    state: MailNotificationServiceState = MailNotificationServiceState.empty)(implicit c: ExecutionContext): Props =
    Props(new MailNotificationServiceActor(id, retryInterval, new MailClient(settings), state))

  def propsWithMailClient(
    id: String,
    retryInterval: FiniteDuration,
    mailClient: MailClient,
    state: MailNotificationServiceState = MailNotificationServiceState.empty)(implicit c: ExecutionContext): Props =
    Props(new MailNotificationServiceActor(id, retryInterval, mailClient, state))
}