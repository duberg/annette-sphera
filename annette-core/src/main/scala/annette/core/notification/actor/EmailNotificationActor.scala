package annette.core.notification.actor

import akka.actor.Props
import annette.core.notification.actor.EmailNotificationActor._
import annette.core.notification.client.EmailClient
import annette.core.notification._
import annette.core.akkaext.actor._
import annette.core.utils.Generator
import javax.mail.AuthenticationFailedException

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{ Failure, Success, Try }

private class EmailNotificationActor(
  val id: ActorId,
  val retryInterval: FiniteDuration,
  val emailClient: EmailClient,
  val initState: EmailNotificationState)(implicit val executor: ExecutionContext)
  extends CqrsActor[EmailNotificationState] with Generator {

  type ClientResult = (EmailNotificationLike, Try[EmailClient.Response])
  type ClientSuccess = (EmailNotificationLike, EmailClient.Response)
  type ClientFailure = (EmailNotificationLike, Throwable)

  def send(x: EmailNotificationLike): ClientResult =
    x -> emailClient.send(
      to = x.email,
      subject = x.subject,
      message = x.message)

  def processResults(state: EmailNotificationState, x: Seq[ClientResult]): Unit = {
    changeState((state /: x) {
      case (s, (n, Success(_))) => s.updated(DeletedNotificationEvt(n.id))
      case (s, (n, Failure(_))) => s.updated(UpdatedRetryEvt(n.id, n.retry - 1))
    })
  }

  def hideCredentials: PartialFunction[(EmailNotificationLike, Any), (EmailNotificationLike, Any)] = {
    case (n: SendPasswordToEmailNotification, x) if !emailClient.settings.debug =>
      (n.copy(password = hide(n.password)), x)
    case x => x
  }

  def processFailures(failures: Seq[ClientFailure]): Unit = {
    failures
      .map(hideCredentials)
      .foreach {
        case (n: SendPasswordToEmailNotification, e) => log.warning(s"Failed ${n.getClass.getSimpleName} [${n.id}, email:${n.email}, password: ${n.password}] [$e]")
        case (n, e) => log.warning(s"Failed ${n.getClass.getSimpleName} [${n.id}, email:${n.email}] [$e]")
      }
  }

  def processSuccesses(success: Seq[ClientSuccess]): Unit = {
    success
      .map(hideCredentials)
      .foreach {
        case (n: SendPasswordToEmailNotification, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, email:${n.email}, password: ${n.password}]")
        case (n, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, email:${n.email}]")
      }
  }

  def notify(state: EmailNotificationState): Unit = {
    if (state.nonEmpty) {
      val notifications = state.v.values.toSeq
      emailClient.connect() match {
        case Success(_) =>
          val results: Seq[ClientResult] = notifications.map(send)

          val success: Seq[ClientSuccess] = results.collect {
            case (n, Success(r: EmailClient.Response)) => (n, r)
          }

          val failures: Seq[ClientFailure] = results.collect {
            case (n, Failure(e: EmailClient.Exception)) => (n, e)
          }

          processResults(state, results)
          processFailures(failures)
          processSuccesses(success)
          emailClient.disconnect()
        case Failure(e) =>
          changeState((state /: notifications) {
            case (s, n) => s.updated(UpdatedRetryEvt(n.id, n.retry - 1))
          })
          e match {
            case x: AuthenticationFailedException => log.error(s"Failed credentials [$e]")
            case _ => log.error(s"Failed creating new smtp connection [$e]")
          }
      }
    }
    notifyAfterRetry()
    replyDone()
  }

  def createNotification(state: EmailNotificationState, x: CreateEmailNotificationLike): Unit = {
    val notification = x match {
      case y: CreateSendPasswordToEmailNotification => SendPasswordToEmailNotification(
        id = generateUUID,
        email = y.email,
        subject = y.subject,
        message = y.message,
        password = y.password)
      case y: CreateEmailNotificationLike => EmailNotification(
        id = generateUUID,
        email = y.email,
        subject = y.subject,
        message = y.message)
    }
    changeState(state.updated(CreatedNotificationEvt(notification)))
    sender() ! CreateNotificationSuccess(notification)
  }

  def listNotifications(state: EmailNotificationState): Unit =
    sender() ! NotificationsMap(state.v)

  def behavior(state: EmailNotificationState): Receive = {
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

object EmailNotificationActor {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case object NotifyCmd extends Command
  case class CreateNotificationCmd(x: CreateEmailNotificationLike) extends Command

  case object ListNotifications extends CqrsQuery

  case class CreatedNotificationEvt(x: EmailNotificationLike) extends Event
  case class DeletedNotificationEvt(x: Notification.Id) extends Event
  case class UpdatedRetryEvt(x: Notification.Id, retry: Int) extends Event

  case object Done extends CqrsResponse
  case class CreateNotificationSuccess(x: EmailNotificationLike) extends Response
  case object NotificationAlreadyExists extends CqrsResponse
  case object NotificationNotFound extends CqrsResponse
  case class NotificationsMap(x: Map[Notification.Id, EmailNotificationLike]) extends CqrsResponse

  def props(
    id: ActorId,
    retryInterval: FiniteDuration,
    settings: EmailSettings,
    state: EmailNotificationState = EmailNotificationState.empty)(implicit c: ExecutionContext): Props =
    Props(new EmailNotificationActor(id, retryInterval, new EmailClient(settings), state))

  def propsWithMailClient(
    id: ActorId,
    retryInterval: FiniteDuration,
    emailClient: EmailClient,
    state: EmailNotificationState = EmailNotificationState.empty)(implicit c: ExecutionContext): Props =
    Props(new EmailNotificationActor(id, retryInterval, emailClient, state))
}