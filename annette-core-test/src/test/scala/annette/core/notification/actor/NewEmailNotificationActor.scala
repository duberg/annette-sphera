package annette.core.notification.actor

import java.net.{ ConnectException, SocketException }

import akka.actor.ActorRef
import annette.core.notification.client.EmailClient
import annette.core.notification._
import annette.core.akkaext.actor.ActorId
import annette.core.test.PersistenceSpec
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.AsyncMockFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

trait NewEmailNotificationActor extends NotificationConfig with AsyncMockFactory { _: PersistenceSpec =>
  import EmailNotificationActor._

  lazy val emailNotificationConfig: MailNotificationEntry =
    ConfigFactory.load().getConfig("annette").mailNotificationEntry

  def createMailNotification(a: ActorRef, x: CreateEmailNotificationLike): Future[EmailNotificationLike] =
    ask(a, CreateNotificationCmd(x))
      .mapTo[CreateNotificationSuccess]
      .map(_.x)

  def getMailNotifications(a: ActorRef): Future[Map[Notification.Id, EmailNotificationLike]] =
    ask(a, ListNotifications).mapTo[NotificationsMap].map(_.x)

  def mailNotify(a: ActorRef): Future[Any] =
    ask(a, NotifyCmd)

  def generateCreateSendPasswordToEmailNotification(id: Notification.Id = generateUUID): Future[CreateEmailNotificationLike] = Future {
    CreateSendPasswordToEmailNotification(
      email = generateEmail,
      subject = generateString(),
      message = generateString(),
      password = generatePassword)
  }

  def generateCreateEmailNotification(id: Notification.Id = generateUUID): Future[CreateEmailNotificationLike] = Future {
    CreateEmailNotification(
      email = generateEmail,
      subject = generateString(),
      message = generateString())
  }

  def newEmailNotificationActor(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(EmailNotificationActor.props(id, emailNotificationConfig.retryInterval, emailNotificationConfig.mail), id.name)
  }

  def stubbedEmailClient(settings: EmailSettings): EmailClient = {
    val x = stub[EmailClient]
    x.settings _ when () returns settings
    x.connect _ when () returns Success(EmailClient.Connected)
    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
      Success(EmailClient.SendSuccess)
    }
    x
  }

  def stubbedSocketExceptionEmailClient(settings: EmailSettings): EmailClient = {
    val x = stub[EmailClient]
    x.settings _ when () returns settings
    x.connect _ when () returns Success(EmailClient.Connected)
    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
      Failure(new SocketException("Socket exception"))
    }
    x
  }

  def stubbedConnectionRefusedEmailClient(settings: EmailSettings): EmailClient = {
    val x = stub[EmailClient]
    x.settings _ when () returns settings
    x.connect _ when () returns Failure(new ConnectException("Connection refused"))
    x
  }

  def stubbedSocketExceptionEmailClientInDebug(settings: EmailSettings): EmailClient = {
    val s = settings.copy(debug = true)
    val x = stub[EmailClient]
    x.settings _ when () returns s
    x.connect _ when () returns Success(EmailClient.Connected)
    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
      Failure(new SocketException("Socket exception"))
    }
    x
  }

  def newStubbedEmailNotificationActor(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(EmailNotificationActor.propsWithMailClient(
      id = id,
      retryInterval = 1 hour,
      emailClient = stubbedEmailClient(emailNotificationConfig.mail)), id.name)
  }

  def newStubbedSocketExceptionEmailNotificationActor(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(EmailNotificationActor.propsWithMailClient(
      id = id,
      retryInterval = 1 hour,
      emailClient = stubbedSocketExceptionEmailClient(emailNotificationConfig.mail)), id.name)
  }

  def newStubbedSocketExceptionEmailNotificationActorInDebug(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(EmailNotificationActor.propsWithMailClient(
      id = id,
      retryInterval = 1 hour,
      emailClient = stubbedSocketExceptionEmailClientInDebug(emailNotificationConfig.mail)), id.name)
  }

  def newStubbedConnectionRefusedEmailNotificationActor(id: ActorId = generateActorId, state: EmailNotificationState = EmailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(EmailNotificationActor.propsWithMailClient(
      id = id,
      retryInterval = 1 hour,
      emailClient = stubbedConnectionRefusedEmailClient(emailNotificationConfig.mail)), id.name)
  }
}
