package annette.imc.notification.actor

import java.net.ConnectException

import akka.actor.ActorRef
import akka.pattern.ask
import annette.core.test.PersistenceSpec
import annette.imc.notification.actor.SmsNotificationServiceActor._
import annette.imc.notification.client.SmsClient
import annette.imc.notification.model.{ SmsNotification, _ }
import annette.imc.notification.{ NotificationConfig, SmsSettings }
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest._

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait NewSmsNotificationService extends NotificationConfig with AsyncMockFactory { _: PersistenceSpec =>
  lazy val smsNotificationConfig: SmsNotificationEntry =
    ConfigFactory.load().getConfig("annette").smsNotificationEntry

  def addSmsNotification(a: ActorRef, x: SmsNotification): Future[Any] =
    ask(a, AddNotificationCmd(x))

  def deleteSmsNotification(a: ActorRef, id: Notification.Id): Future[Any] =
    ask(a, DeleteNotificationCmd(id))

  def getSmsNotifications(a: ActorRef): Future[Map[Notification.Id, SmsNotification]] =
    ask(a, GetNotifications).mapTo[NotificationsRes].map(_.x)

  def smsNotify(a: ActorRef): Future[Any] =
    ask(a, Notify)

  def updateSmsRetry(a: ActorRef, id: Notification.Id, r: Int): Future[Any] =
    ask(a, UpdateRetryCmd(id, r))

  def generateSmsNotificationPassword(id: Notification.Id = generateUUID): Future[SmsNotification] = Future {
    SmsNotification.Password(
      id = id,
      phone = generatePhone,
      password = generatePassword,
      language = "RU",
      retry = 2)
  }

  def generateSmsNotificationVerification(id: Notification.Id = generateUUID): Future[SmsNotification] = Future {
    SmsNotification.Verification(
      id = id,
      phone = generatePhone,
      code = generatePinString,
      language = "RU",
      retry = 2)
  }

  def generateSmsNotificationToExpertise(id: Notification.Id = generateUUID): Future[SmsNotification] = Future {
    SmsNotification.ToExpertise(
      id = id,
      phone = generatePhone,
      language = "RU",
      retry = 2)
  }

  def generateSmsNotificationToReview(id: Notification.Id = generateUUID): Future[SmsNotification] = Future {
    SmsNotification.ToReview(
      id = id,
      phone = generatePhone,
      language = "RU",
      retry = 2)
  }

  def newSmsNotificationService(id: String = generateId, state: SmsNotificationServiceState = SmsNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(SmsNotificationServiceActor.props(id, smsNotificationConfig.retryInterval, smsNotificationConfig.sms), id)
  }

  def stubbedSmsClient(settings: SmsSettings): SmsClient = {
    val x = stub[SmsClient]
    x.settings _ when () returns settings
    x.send _ when (*, *, *, *) onCall { (id: Notification.Id, to: String, message: String, from: String) =>
      new SmsClient(settings).prepare(id, to, message, from)
      Success(SmsClient.Success)
    }
    x
  }

  def stubbedConnectionRefusedSmsClient(settings: SmsSettings): SmsClient = {
    val x = stub[SmsClient]
    x.settings _ when () returns settings
    x.send _ when (*, *, *, *) onCall { (id: Notification.Id, to: String, message: String, from: String) =>
      new SmsClient(settings).prepare(id, to, message, from)
      Failure(new ConnectException("Sms server connection refused"))
    }
    x
  }

  def stubbedSmsClientInDebug(settings: SmsSettings): SmsClient = {
    val s = settings.copy(debug = true)
    val x = stub[SmsClient]
    x.settings _ when () returns s
    x.send _ when (*, *, *, *) onCall { (id: Notification.Id, to: String, message: String, from: String) =>
      new SmsClient(s).prepare(id, to, message, from)
      Success(SmsClient.Success)
    }
    x
  }

  def stubbedConnectionRefusedSmsClientInDebug(settings: SmsSettings): SmsClient = {
    val s = settings.copy(debug = true)
    val x = stub[SmsClient]
    x.settings _ when () returns s
    x.send _ when (*, *, *, *) onCall { (id: Notification.Id, to: String, message: String, from: String) =>
      new SmsClient(s).prepare(id, to, message, from)
      Failure(new ConnectException("Sms server connection refused"))
    }
    x
  }

  def newStubbedSmsNotificationService(id: String = generateId, state: SmsNotificationServiceState = SmsNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(SmsNotificationServiceActor.propsWithSmsClient(
      id,
      smsNotificationConfig.retryInterval,
      stubbedSmsClient(smsNotificationConfig.sms)), id)
  }

  def newStubbedConnectionRefusedSmsNotificationService(id: String = generateId, state: SmsNotificationServiceState = SmsNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(SmsNotificationServiceActor.propsWithSmsClient(
      id,
      smsNotificationConfig.retryInterval,
      stubbedConnectionRefusedSmsClient(smsNotificationConfig.sms)), id)
  }

  def newStubbedSmsNotificationServiceInDebug(id: String = generateId, state: SmsNotificationServiceState = SmsNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(SmsNotificationServiceActor.propsWithSmsClient(
      id,
      smsNotificationConfig.retryInterval,
      stubbedSmsClientInDebug(smsNotificationConfig.sms)), id)
  }

  def newStubbedConnectionRefusedSmsNotificationServiceInDebug(id: String = generateId, state: SmsNotificationServiceState = SmsNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(SmsNotificationServiceActor.propsWithSmsClient(
      id,
      smsNotificationConfig.retryInterval,
      stubbedConnectionRefusedSmsClientInDebug(smsNotificationConfig.sms)), id)
  }
}