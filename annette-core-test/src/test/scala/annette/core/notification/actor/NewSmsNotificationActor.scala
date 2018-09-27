//package annette.core.notification.actor
//
//import java.net.ConnectException
//
//import akka.actor.ActorRef
//import annette.core.notification.client.SmsClient
//import annette.core.notification.{ Notification, NotificationConfig, SmsNotificationLike, SmsSettings }
//import annette.core.akkaext.actor.ActorId
//import annette.core.test.PersistenceSpec
//import com.typesafe.config.ConfigFactory
//import org.scalamock.scalatest.AsyncMockFactory
//
//import scala.concurrent.Future
//import scala.util.{ Failure, Success }
//
//trait NewSmsNotificationActor extends NotificationConfig with AsyncMockFactory { _: PersistenceSpec =>
//  import SmsNotificationActor._
//
//  lazy val smsNotificationConfig: SmsNotificationEntry =
//    ConfigFactory.load().getConfig("annette").smsNotificationEntry
//
//  def createSmsNotification(a: ActorRef, x: SmsNotificationLike): Future[Any] =
//    ask(a, CreateNotificationCmd(x))
//
//  def getSmsNotifications(a: ActorRef): Future[Map[Notification.Id, SmsNotificationLike]] =
//    ask(a, ListNotifications).mapTo[NotificationMap].map(_.x)
//
//  def smsNotify(a: ActorRef): Future[Any] =
//    ask(a, NotifyCmd)
//
//  def generateSmsNotificationPassword(id: Notification.Id = generateUUID): Future[SendPasswordToPhone] = Future {
//    SendPasswordToPhone(
//      id = id,
//      phone = generatePhone,
//      subject = generateString(),
//      message = generateString(),
//      password = generatePassword,
//      retry = 2)
//  }
//
//  def generateSmsNotificationVerification(id: Notification.Id = generateUUID): Future[SmsVerification] = Future {
//    SmsVerification(
//      id = id,
//      phone = generatePhone,
//      subject = generateString(),
//      message = generateString(),
//      code = generatePinString,
//      retry = 2)
//  }
//
//  def generateSmsNotificationTextMessage(id: Notification.Id = generateUUID): Future[SmsNotification] = Future {
//    SmsNotification(
//      id = id,
//      phone = generatePhone,
//      subject = generateString(),
//      message = generateString(),
//      retry = 2)
//  }
//
//  def newSmsNotificationActor(
//    id: ActorId = generateActorId,
//    state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(SmsNotificationActor.props(id, smsNotificationConfig.retryInterval, smsNotificationConfig.sms), id.name)
//  }
//
//  def stubbedSmsClient(settings: SmsSettings): SmsClient = {
//    val x = stub[SmsClient]
//    x.settings _ when () returns settings
//    x.send _ when (*, *, *, *) onCall { (id: Notification.Id, to: String, message: String, from: String) =>
//      new SmsClient(settings).prepare(id, to, message, from)
//      Success(SmsClient.Success)
//    }
//    x
//  }
//
//  def stubbedConnectionRefusedSmsClient(settings: SmsSettings): SmsClient = {
//    val x = stub[SmsClient]
//    x.settings _ when () returns settings
//    x.send _ when (*, *, *, *) onCall { (id: Notification.Id, to: String, message: String, from: String) =>
//      new SmsClient(settings).prepare(id, to, message, from)
//      Failure(new ConnectException("Sms server connection refused"))
//    }
//    x
//  }
//
//  def stubbedSmsClientInDebug(settings: SmsSettings): SmsClient = {
//    val s = settings.copy(debug = true)
//    val x = stub[SmsClient]
//    x.settings _ when () returns s
//    x.send _ when (*, *, *, *) onCall { (id: Notification.Id, to: String, message: String, from: String) =>
//      new SmsClient(s).prepare(id, to, message, from)
//      Success(SmsClient.Success)
//    }
//    x
//  }
//
//  def stubbedConnectionRefusedSmsClientInDebug(settings: SmsSettings): SmsClient = {
//    val s = settings.copy(debug = true)
//    val x = stub[SmsClient]
//    x.settings _ when () returns s
//    x.send _ when (*, *, *, *) onCall { (id: Notification.Id, to: String, message: String, from: String) =>
//      new SmsClient(s).prepare(id, to, message, from)
//      Failure(new ConnectException("Sms server connection refused"))
//    }
//    x
//  }
//
//  def newStubbedSmsNotificationActor(id: ActorId = generateActorId, state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(SmsNotificationActor.propsWithSmsClient(
//      id,
//      smsNotificationConfig.retryInterval,
//      stubbedSmsClient(smsNotificationConfig.sms)), id.name)
//  }
//
//  def newStubbedConnectionRefusedSmsNotificationActor(id: ActorId = generateActorId, state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(SmsNotificationActor.propsWithSmsClient(
//      id,
//      smsNotificationConfig.retryInterval,
//      stubbedConnectionRefusedSmsClient(smsNotificationConfig.sms)), id.name)
//  }
//
//  def newStubbedSmsNotificationActorInDebug(id: ActorId = generateActorId, state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(SmsNotificationActor.propsWithSmsClient(
//      id,
//      smsNotificationConfig.retryInterval,
//      stubbedSmsClientInDebug(smsNotificationConfig.sms)), id.name)
//  }
//
//  def newStubbedConnectionRefusedSmsNotificationManagerInDebug(id: ActorId = generateActorId, state: SmsNotificationState = SmsNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(SmsNotificationActor.propsWithSmsClient(
//      id,
//      smsNotificationConfig.retryInterval,
//      stubbedConnectionRefusedSmsClientInDebug(smsNotificationConfig.sms)), id.name)
//  }
//}