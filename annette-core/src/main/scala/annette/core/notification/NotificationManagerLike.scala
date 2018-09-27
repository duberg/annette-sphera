package annette.core.notification

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core._
import annette.core.model.EntityType
import annette.core.notification.actor._
import annette.core.akkaext.actor.CqrsResponse
import annette.core.security.verification._
import annette.core.utils.Generator

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

trait NotificationManagerLike extends Generator {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  protected def notificationManagerActor: ActorRef

  def listSmsNotifications: Future[Seq[SmsNotificationLike]] =
    ask(notificationManagerActor, SmsNotificationActor.ListNotifications)
      .mapTo[SmsNotificationActor.NotificationMap]
      .map(_.x.values.toSeq)

  def listVerifications: Future[Seq[Verification]] =
    ask(notificationManagerActor, Verification.ListVerifications)
      .mapTo[Verification.VerificationMap]
      .map(_.x.values.toSeq)

  /**
   * Полностью асинхронный быстрый сброс уведомлений.
   * При сбросе сообщения [[Verification]] автоматически создается верификация.
   */
  def push(x: CreateNotification): Unit = x match {
    case y: CreateEmailNotificationLike => notificationManagerActor ! EmailNotificationActor.CreateNotificationCmd(y)
    case y: CreateSmsNotificationLike => notificationManagerActor ! SmsNotificationActor.CreateNotificationCmd(y)
    case y: CreateWebSocketNotificationLike => notificationManagerActor ! WebSocketNotificationActor.NotifyCmd(y)
  }

  def send(x: CreateNotification): Future[CqrsResponse] = {
    x match {
      case y: CreateEmailNotificationLike => notificationManagerActor.ask(EmailNotificationActor.CreateNotificationCmd(y))
      case y: CreateSmsNotificationLike => notificationManagerActor.ask(SmsNotificationActor.CreateNotificationCmd(y))
      case y: CreateWebSocketNotificationLike => notificationManagerActor.ask(WebSocketNotificationActor.NotifyCmd(y))
    }
  }.mapTo[CqrsResponse]

  def createVerification(): Future[Verification] = {
    val x = CreateVerification(
      code = generatePinString,
      duration = 10.minutes)

    ask(notificationManagerActor, Verification.CreateVerificationCmd(x))
      .mapTo[Verification.CreateVerificationSuccess]
      .map(_.x)
  }

  def verify(verificationId: Verification.Id, code: String): Future[Verification.Id] =
    ask(notificationManagerActor, Verification.VerifyCmd(verificationId, code))
      .mapTo[Verification.Response]
      .map {
        case Verification.Done => verificationId
        case Verification.InvalidCode => throw VerificationInvalidCodeException()
        case Verification.VerificationNotFound => throw VerificationNotFoundException(verificationId)
      }
}