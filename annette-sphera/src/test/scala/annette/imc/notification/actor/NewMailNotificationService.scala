package annette.imc.notification.actor

import java.net.{ ConnectException, SocketException }
import java.time.LocalDate

import akka.actor.ActorRef
import akka.pattern.ask
import annette.core.test.PersistenceSpec
import annette.imc.notification.actor.MailNotificationServiceActor._
import annette.imc.notification.client.MailClient
import annette.imc.notification.model.{ MailNotification, _ }
import annette.imc.notification.{ MailSettings, NotificationConfig }
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest._

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait NewMailNotificationService extends NotificationConfig with AsyncMockFactory { _: PersistenceSpec =>
  lazy val mailNotificationConfig: MailNotificationEntry =
    ConfigFactory.load().getConfig("annette").mailNotificationEntry

  def addMailNotification(a: ActorRef, x: MailNotification): Future[Any] =
    ask(a, AddNotificationCmd(x))

  def deleteMailNotification(a: ActorRef, id: Notification.Id): Future[Any] =
    ask(a, DeleteNotificationCmd(id))

  def getMailNotifications(a: ActorRef): Future[Map[Notification.Id, MailNotification]] =
    ask(a, GetNotifications).mapTo[NotificationsRes].map(_.x)

  def mailNotify(a: ActorRef): Future[Any] =
    ask(a, Notify)

  def updateMailRetry(a: ActorRef, id: Notification.Id, r: Int): Future[Any] =
    ask(a, UpdateRetryCmd(id, r))

  def generateMailTemplateParameters: Map[String, String] =
    Map(
      "Date" -> LocalDate.now(),
      "Title" -> generateString(),
      "Expert" -> generateString(),
      "ExpertFull" -> generateString(),
      "Applicant" -> generateString(),
      "ChairmanOfTheExpertCouncilDescription" -> generateString(),
      "ChairmanOfTheExpertCouncil" -> generateString()).mapValues(_.toString)

  def generateMailNotificationPassword(id: Notification.Id = generateUUID): Future[MailNotification] = Future {
    MailNotification.Password(
      id = id,
      email = generateEmail,
      password = generatePassword,
      language = "RU",
      generateMailTemplateParameters,
      retry = 2)
  }

  def generateMailNotificationToExpertise(id: Notification.Id = generateUUID): Future[MailNotification] = Future {
    MailNotification.ToExpertise(
      id = id,
      email = generateEmail,
      language = "RU",
      generateMailTemplateParameters,
      retry = 2)
  }

  def generateMailNotificationToReview(id: Notification.Id = generateUUID): Future[MailNotification] = Future {
    MailNotification.ToReview(
      id = id,
      email = generateEmail,
      language = "RU",
      generateMailTemplateParameters,
      retry = 2)
  }

  def newMailNotificationService(id: String = generateId, state: MailNotificationServiceState = MailNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationServiceActor.props(id, mailNotificationConfig.retryInterval, mailNotificationConfig.mail), id)
  }

  def stubbedMailClient(settings: MailSettings): MailClient = {
    val x = stub[MailClient]
    x.settings _ when () returns settings
    x.connect _ when () returns Success(MailClient.Connected)
    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
      Success(MailClient.SendSuccess)
    }
    x
  }

  def stubbedSocketExceptionMailClient(settings: MailSettings): MailClient = {
    val x = stub[MailClient]
    x.settings _ when () returns settings
    x.connect _ when () returns Success(MailClient.Connected)
    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
      Failure(new SocketException("Socket exception"))
    }
    x
  }

  def stubbedConnectionRefusedMailClient(settings: MailSettings): MailClient = {
    val x = stub[MailClient]
    x.settings _ when () returns settings
    x.connect _ when () returns Failure(new ConnectException("Connection refused"))
    x
  }

  def stubbedSocketExceptionMailClientInDebug(settings: MailSettings): MailClient = {
    val s = settings.copy(debug = true)
    val x = stub[MailClient]
    x.settings _ when () returns s
    x.connect _ when () returns Success(MailClient.Connected)
    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
      Failure(new SocketException("Socket exception"))
    }
    x
  }

  def newStubbedMailNotificationService(id: String = generateId, state: MailNotificationServiceState = MailNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationServiceActor.propsWithMailClient(
      id,
      mailNotificationConfig.retryInterval,
      stubbedMailClient(mailNotificationConfig.mail)), id)
  }

  def newStubbedSocketExceptionMailNotificationService(id: String = generateId, state: MailNotificationServiceState = MailNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationServiceActor.propsWithMailClient(
      id,
      mailNotificationConfig.retryInterval,
      stubbedSocketExceptionMailClient(mailNotificationConfig.mail)), id)
  }

  def newStubbedSocketExceptionMailNotificationServiceInDebug(id: String = generateId, state: MailNotificationServiceState = MailNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationServiceActor.propsWithMailClient(
      id,
      mailNotificationConfig.retryInterval,
      stubbedSocketExceptionMailClientInDebug(mailNotificationConfig.mail)), id)
  }

  def newStubbedConnectionRefusedMailNotificationService(id: String = generateId, state: MailNotificationServiceState = MailNotificationServiceState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationServiceActor.propsWithMailClient(
      id,
      mailNotificationConfig.retryInterval,
      stubbedConnectionRefusedMailClient(mailNotificationConfig.mail)), id)
  }
}
