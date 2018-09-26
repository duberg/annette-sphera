package annette.core.notification

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core._
import annette.core.model.EntityType
import annette.core.notification.actor._
import annette.core.akkaext.actor.CqrsResponse
import annette.core.notification.actor.SmsVerificationActor.VerificationAlreadyExists
import annette.core.utils.Generator

import scala.concurrent.{ ExecutionContext, Future }

trait NotificationManagerLike extends Generator {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  protected def notificationManagerActor: ActorRef

  private def idOrThrow(verificationId: Verification.Id): PartialFunction[SmsVerificationActor.Response, Verification.Id] = {
    case SmsVerificationActor.Done => verificationId
    case SmsVerificationActor.InvalidCode => throw VerificationInvalidCodeException()
    case SmsVerificationActor.VerificationNotFound => throw VerificationNotFoundException(verificationId)
    case SmsVerificationActor.VerificationAlreadyExists => throw VerificationAlreadyExistsException(verificationId)
  }

  def getSmsNotifications: Future[Seq[SmsNotification]] =
    ask(notificationManagerActor, SmsNotificationActor.GetNotifications)
      .mapTo[SmsNotificationActor.NotificationMap]
      .map(_.x.values.toSeq)

  def getSmsVerifications: Future[Seq[SmsVerification]] =
    ask(notificationManagerActor, SmsVerificationActor.GetVerifications)
      .mapTo[SmsVerificationActor.VerificationMap]
      .map(_.x.values.toSeq)

  /**
   * Полностью асинхронный быстрый сброс уведомлений.
   * При сбросе сообщения [[SmsNotification.Verification]] автоматически создается верификация.
   */
  def push(x: Notification): Unit = x match {
    case y: MailNotification => notificationManagerActor ! MailNotificationActor.CreateNotificationCmd(y)
    case y: SmsNotification.Verification => notificationManagerActor ! SmsVerificationActor.CreateVerificationCmd(
      SmsVerification(
        id = y.id,
        notification = y))
    case y: SmsNotification => notificationManagerActor ! SmsNotificationActor.CreateNotificationCmd(y)
    case y: WebSocketNotification => notificationManagerActor ! WebSocketNotificationActor.NotifyCmd(y)
  }

  def send(x: Notification): Future[CqrsResponse] = {
    x match {
      case y: MailNotification => notificationManagerActor.ask(MailNotificationActor.CreateNotificationCmd(y))
      case y: SmsNotification.Verification => notificationManagerActor.ask(SmsVerificationActor.CreateVerificationCmd(
        SmsVerification(
          id = y.id,
          notification = y)))
      case y: SmsNotification => notificationManagerActor.ask(SmsNotificationActor.CreateNotificationCmd(y))
      case y: WebSocketNotification => notificationManagerActor.ask(WebSocketNotificationActor.NotifyCmd(y))
    }
  }.mapTo[CqrsResponse]

  def verify(verificationId: Verification.Id, code: String): Future[Verification.Id] =
    ask(notificationManagerActor, SmsVerificationActor.VerifyCmd(verificationId, code))
      .mapTo[SmsVerificationActor.Response]
      .map(idOrThrow(verificationId))
}