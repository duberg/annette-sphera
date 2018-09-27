//package annette.core.notification.actor
//
//import java.net.{ ConnectException, SocketException }
//
//import akka.actor.ActorRef
//import annette.core.notification.client.EmailClient
//import annette.core.notification.{ EmailNotificationLike, EmailSettings, Notification, NotificationConfig }
//import annette.core.akkaext.actor.ActorId
//import annette.core.test.PersistenceSpec
//import com.typesafe.config.ConfigFactory
//import org.scalamock.scalatest.AsyncMockFactory
//
//import scala.concurrent.Future
//import scala.concurrent.duration._
//import scala.util.{ Failure, Success }
//
//trait NewEmailNotificationActor extends NotificationConfig with AsyncMockFactory { _: PersistenceSpec =>
//  import EmailNotificationActor._
//
//  lazy val mailNotificationConfig: MailNotificationEntry =
//    ConfigFactory.load().getConfig("annette").mailNotificationEntry
//
//  def createMailNotification(a: ActorRef, x: EmailNotificationLike): Future[Any] =
//    ask(a, CreateNotificationCmd(x))
//
//  def getMailNotifications(a: ActorRef): Future[Map[Notification.Id, EmailNotificationLike]] =
//    ask(a, ListNotifications).mapTo[NotificationsMap].map(_.x)
//
//  def mailNotify(a: ActorRef): Future[Any] =
//    ask(a, NotifyCmd)
//
//  def generateMailNotificationPassword(id: Notification.Id = generateUUID): Future[EmailNotificationLike] = Future {
//    SendPasswordToEmail(
//      id = id,
//      email = generateEmail,
//      subject = generateString(),
//      message = generateString(),
//      password = generatePassword,
//      retry = 2)
//  }
//
//  def generateMailNotificationTextMessage(id: Notification.Id = generateUUID): Future[EmailNotificationLike] = Future {
//    MailNotification(
//      id = id,
//      email = generateEmail,
//      subject = generateString(),
//      message = generateString(),
//      retry = 2)
//  }
//
//  def newMailNotificationActor(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(EmailNotificationActor.props(id, mailNotificationConfig.retryInterval, mailNotificationConfig.mail), id.name)
//  }
//
//  def stubbedMailClient(settings: EmailSettings): EmailClient = {
//    val x = stub[EmailClient]
//    x.settings _ when () returns settings
//    x.connect _ when () returns Success(EmailClient.Connected)
//    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
//      Success(EmailClient.SendSuccess)
//    }
//    x
//  }
//
//  def stubbedSocketExceptionMailClient(settings: EmailSettings): EmailClient = {
//    val x = stub[EmailClient]
//    x.settings _ when () returns settings
//    x.connect _ when () returns Success(EmailClient.Connected)
//    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
//      Failure(new SocketException("Socket exception"))
//    }
//    x
//  }
//
//  def stubbedConnectionRefusedMailClient(settings: EmailSettings): EmailClient = {
//    val x = stub[EmailClient]
//    x.settings _ when () returns settings
//    x.connect _ when () returns Failure(new ConnectException("Connection refused"))
//    x
//  }
//
//  def stubbedSocketExceptionMailClientInDebug(settings: EmailSettings): EmailClient = {
//    val s = settings.copy(debug = true)
//    val x = stub[EmailClient]
//    x.settings _ when () returns s
//    x.connect _ when () returns Success(EmailClient.Connected)
//    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
//      Failure(new SocketException("Socket exception"))
//    }
//    x
//  }
//
//  def newStubbedMailNotificationActor(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(EmailNotificationActor.propsWithMailClient(
//      id = id,
//      retryInterval = 1 hour,
//      emailClient = stubbedMailClient(mailNotificationConfig.mail)), id.name)
//  }
//
//  def newStubbedSocketExceptionMailNotificationActor(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(EmailNotificationActor.propsWithMailClient(
//      id = id,
//      retryInterval = 1 hour,
//      emailClient = stubbedSocketExceptionMailClient(mailNotificationConfig.mail)), id.name)
//  }
//
//  def newStubbedSocketExceptionMailNotificationActorInDebug(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(EmailNotificationActor.propsWithMailClient(
//      id = id,
//      retryInterval = 1 hour,
//      emailClient = stubbedSocketExceptionMailClientInDebug(mailNotificationConfig.mail)), id.name)
//  }
//
//  def newStubbedConnectionRefusedMailNotificationActor(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
//    system.actorOf(EmailNotificationActor.propsWithMailClient(
//      id = id,
//      retryInterval = 1 hour,
//      emailClient = stubbedConnectionRefusedMailClient(mailNotificationConfig.mail)), id.name)
//  }
//}
