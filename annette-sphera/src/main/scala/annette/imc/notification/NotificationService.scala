package annette.imc.notification

import java.util.UUID

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.util.Timeout
import annette.core.utils.Generator
import annette.imc.model.{ Ap, ApStatus, UpdateBulletin }
import annette.imc.notification.actor.{ MailNotificationServiceActor, NotificationServiceActor, SmsNotificationServiceActor, SmsVerificationServiceActor }
import annette.imc.notification.model._
import com.typesafe.config.Config

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Обертка Future API для [[NotificationServiceActor]].
 */
class NotificationService(a: ActorRef)(implicit val executionContext: ExecutionContext, val timeout: Timeout)
  extends Generator {
  def getSmsNotifications: Future[Seq[SmsNotification]] =
    ask(a, SmsNotificationServiceActor.GetNotifications)
      .mapTo[SmsNotificationServiceActor.NotificationsRes]
      .map(_.x.values.toSeq)

  def addNotificationAsync(x: Notification): Unit = x match {
    case y: MailNotification => a ! MailNotificationServiceActor.AddNotificationCmd(y)
    case y: SmsNotification => a ! SmsNotificationServiceActor.AddNotificationCmd(y)
  }

  def smsNotify: Future[String] =
    ask(a, SmsNotificationServiceActor.Notify)
      .map(_.toString)

  def getSmsVerifications: Future[Seq[SmsVerification]] =
    ask(a, SmsVerificationServiceActor.GetVerifications)
      .mapTo[SmsVerificationServiceActor.VerificationsRes]
      .map(_.x.values.toSeq)

  def addSmsVerificationVoted(phone: String, apId: Ap.Id, bulletin: UpdateBulletin, language: String): Future[SmsVerification.Voted] = {
    val x = SmsVerification.Voted(
      id = UUID.randomUUID(),
      code = generatePinString,
      phone = phone,
      apId = apId,
      bulletin = bulletin,
      language = language)
    ask(a, SmsVerificationServiceActor.AddVerificationCmd(x))
      .map(_ => x)
  }

  def addSmsVerificationStatus(phone: String, language: String): Future[SmsVerification.Status] = {
    val x = SmsVerification.Status(
      id = UUID.randomUUID(),
      code = generatePinString,
      phone = phone,
      language = language)
    ask(a, SmsVerificationServiceActor.AddVerificationCmd(x))
      .map(_ => x)
  }

  def smsVerify(id: Verification.Id, code: String): Future[String] =
    ask(a, SmsVerificationServiceActor.VerifyCmd(id, code))
      .map(_.toString)
}

object NotificationService {
  type Id = String

  val NotificationServiceName = s"notification-service"
  val NotificationServiceId = s"$NotificationServiceName-v4.3"

  def apply(apsActor: ActorRef, config: Config)(implicit s: ActorSystem, n: ExecutionContext, m: Timeout): NotificationService = {
    val a = s.actorOf(
      NotificationServiceActor.props(
        id = NotificationServiceId,
        apsActor = apsActor,
        config = config),
      NotificationServiceName)
    new NotificationService(a)
  }
}
