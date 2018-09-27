package annette.core.notification

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core._
import annette.core.model.EntityType
import annette.core.notification.actor._
import annette.core.akkaext.actor.CqrsResponse
import annette.core.security.verification._
import annette.core.security.verification.VerificationActor.{CreateVerificationSuccess, VerificationAlreadyExists}
import annette.core.utils.Generator

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

trait NotificationManagerLike extends Generator {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  protected def notificationManagerActor: ActorRef

  private def idOrThrow(verificationId: Notification.Id): PartialFunction[VerificationActor.Response, Notification.Id] = {
    case VerificationActor.Done => verificationId
    case VerificationActor.InvalidCode => throw VerificationInvalidCodeException()
    case VerificationActor.VerificationNotFound => throw VerificationNotFoundException(verificationId)
    case VerificationActor.VerificationAlreadyExists => throw VerificationAlreadyExistsException(verificationId)
  }

  def listSmsNotifications: Future[Seq[SmsNotificationLike]] =
    ask(notificationManagerActor, SmsNotificationActor.GetNotifications)
      .mapTo[SmsNotificationActor.NotificationMap]
      .map(_.x.values.toSeq)

  def listVerifications: Future[Seq[Verification]] =
    ask(notificationManagerActor, VerificationActor.ListVerifications)
      .mapTo[VerificationActor.VerificationMap]
      .map(_.x.values.toSeq)

  /**
   * Полностью асинхронный быстрый сброс уведомлений.
   * При сбросе сообщения [[Verification]] автоматически создается верификация.
   */
  def push(x: CreateNotification): Unit = x match {
    case y: CreateEmailNotification => notificationManagerActor ! EmailNotificationActor.CreateNotificationCmd(y)
    case y: CreateSmsNotification => notificationManagerActor ! SmsNotificationActor.CreateNotificationCmd(y)
    case y: CreateWebSocketNotification => notificationManagerActor ! WebSocketNotificationActor.NotifyCmd(y)
  }

  def send(x: CreateNotification): Future[CqrsResponse] = {
    x match {
      case y: CreateEmailNotification => notificationManagerActor.ask(EmailNotificationActor.CreateNotificationCmd(y))
      case y: CreateSmsNotification => notificationManagerActor.ask(SmsNotificationActor.CreateNotificationCmd(y))
      case y: CreateWebSocketNotification => notificationManagerActor.ask(WebSocketNotificationActor.NotifyCmd(y))
    }
  }.mapTo[CqrsResponse]


  def createVerification(): Future[Verification] = {
    val x = CreateVerification(
      code = generatePinString,
      duration = 10.minutes
    )

    ask(notificationManagerActor, VerificationActor.CreateVerificationCmd(x))
      .mapTo[VerificationActor.Response]
      .map({
        case VerificationAlreadyExists => throw ???
        case CreateVerificationSuccess(x) => x
      })
  }

  def verify(verificationId: Verification.Id, code: String): Future[Verification.Id] =
    ask(notificationManagerActor, VerificationActor.VerifyCmd(verificationId, code))
      .mapTo[VerificationActor.Response]
      .map(idOrThrow(verificationId))
}