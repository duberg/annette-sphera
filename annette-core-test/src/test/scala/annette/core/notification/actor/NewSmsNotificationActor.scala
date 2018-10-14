package annette.core.notification.actor

import java.net.ConnectException

import akka.actor.ActorRef
import annette.core.notification._
import annette.core.notification.client.SmsClient
import annette.core.test.PersistenceSpec
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.AsyncMockFactory

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait NewSmsNotificationActor extends NotificationConfig with AsyncMockFactory { _: PersistenceSpec =>
  import SmsNotificationActor._

  lazy val smsNotificationConfig: SmsNotificationEntry =
    ConfigFactory.load().getConfig("annette").smsNotificationEntry

  def createSmsNotification(a: ActorRef, x: CreateSmsNotificationLike) =
    ask(a, CreateNotificationCmd(x))
      .mapTo[CreateNotificationSuccess]
      .map(_.x)

  def listSmsNotifications(a: ActorRef): Future[Map[Notification.Id, SmsNotificationLike]] =
    ask(a, ListNotifications).mapTo[NotificationMap].map(_.x)

  def smsNotify(a: ActorRef): Future[Any] =
    ask(a, NotifyCmd)

  def generateCreateSendPasswordToPhoneNotification(id: Notification.Id = generateUUID): Future[CreateSendPasswordToPhoneNotification] = Future {
    CreateSendPasswordToPhoneNotification(
      phone = generatePhone,
      subject = generateString(),
      message = generateString(),
      password = generatePassword)
  }

  def generateCreateVerifyBySmsNotification(id: Notification.Id = generateUUID): Future[CreateVerifyBySmsNotification] = Future {
    CreateVerifyBySmsNotification(
      phone = generatePhone,
      subject = generateString(),
      message = generateString(),
      code = generatePinString)
  }

  def generateCreateSmsNotification(id: Notification.Id = generateUUID): Future[CreateSmsNotification] = Future {
    CreateSmsNotification(
      phone = generatePhone,
      subject = generateString(),
      message = generateString())
  }

  def newSmsNotificationActor(
    id: String = generateString(),
    state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(
      props = SmsNotificationActor.props(smsNotificationConfig.retryInterval, smsNotificationConfig.sms),
      name = id)
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

  def newStubbedSmsNotificationActor(id: String = generateString(), state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(SmsNotificationActor.propsWithSmsClient(
      smsNotificationConfig.retryInterval,
      stubbedSmsClient(smsNotificationConfig.sms)), id)
  }

  def newStubbedConnectionRefusedSmsNotificationActor(id: String = generateString(), state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(SmsNotificationActor.propsWithSmsClient(
      smsNotificationConfig.retryInterval,
      stubbedConnectionRefusedSmsClient(smsNotificationConfig.sms)), id)
  }

  def newStubbedSmsNotificationActorInDebug(id: String = generateString(), state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(SmsNotificationActor.propsWithSmsClient(
      smsNotificationConfig.retryInterval,
      stubbedSmsClientInDebug(smsNotificationConfig.sms)), id)
  }

  def newStubbedConnectionRefusedSmsNotificationManagerInDebug(id: String = generateString(), state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(SmsNotificationActor.propsWithSmsClient(
      smsNotificationConfig.retryInterval,
      stubbedConnectionRefusedSmsClientInDebug(smsNotificationConfig.sms)), id)
  }
}