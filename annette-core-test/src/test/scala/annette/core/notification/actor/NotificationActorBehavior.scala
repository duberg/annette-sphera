package annette.core.notification.actor

import akka.actor.ActorRef
import annette.core.notification._
import annette.core.test.PersistenceSpec

import scala.concurrent.Future

trait NotificationActorBehavior extends NewSmsNotificationActor
  with NewEmailNotificationActor { _: PersistenceSpec =>
  def smsNotificationActor(): Unit = {
    createNotificationBehavior(newSmsNotificationActor(), generateCreateSendPasswordToPhoneNotification())
    createNotificationBehavior(newSmsNotificationActor(), generateCreateVerifyBySmsNotification())
    createNotificationBehavior(newSmsNotificationActor(), generateCreateSmsNotification())

    notifyBehavior(
      newActor = newStubbedSmsNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationActor(),
      generate = generateCreateSendPasswordToPhoneNotification())

    notifyBehavior(
      newActor = newStubbedSmsNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationActor(),
      generate = generateCreateVerifyBySmsNotification())

    notifyBehavior(
      newStubbedSmsNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationActor(),
      generate = generateCreateSmsNotification())

    /**
     * In debug mode
     */
    notifyBehavior(
      newActor = newStubbedSmsNotificationActorInDebug(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationManagerInDebug(),
      generate = generateCreateSendPasswordToPhoneNotification())

    notifyBehavior(
      newActor = newStubbedSmsNotificationActorInDebug(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationManagerInDebug(),
      generate = generateCreateVerifyBySmsNotification())

    notifyBehavior(
      newStubbedSmsNotificationActorInDebug(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationManagerInDebug(),
      generate = generateCreateSmsNotification())
  }

  def mailNotificationActor(): Unit = {
    createNotificationBehavior(newEmailNotificationActor(), generateCreateSendPasswordToEmailNotification())
    createNotificationBehavior(newEmailNotificationActor(), generateCreateEmailNotification())

    notifyBehavior(
      newActor = newStubbedEmailNotificationActor(),
      newActorWithFail = newStubbedSocketExceptionEmailNotificationActor(),
      generate = generateCreateSendPasswordToEmailNotification())

    notifyBehavior(
      newActor = newStubbedEmailNotificationActor(),
      newActorWithFail = newStubbedSocketExceptionEmailNotificationActor(),
      generate = generateCreateEmailNotification())

    notifyBehavior(
      newActor = newStubbedEmailNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedEmailNotificationActor(),
      generate = generateCreateSendPasswordToEmailNotification())

    notifyBehavior(
      newActor = newStubbedEmailNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedEmailNotificationActor(),
      generate = generateCreateEmailNotification())

    /**
     * In debug mode
     */
    notifyBehavior(
      newActor = newStubbedEmailNotificationActor(),
      newActorWithFail = newStubbedSocketExceptionEmailNotificationActorInDebug(),
      generate = generateCreateSendPasswordToEmailNotification())

    notifyBehavior(
      newActor = newStubbedEmailNotificationActor(),
      newActorWithFail = newStubbedSocketExceptionEmailNotificationActorInDebug(),
      generate = generateCreateEmailNotification())
  }

  def createNotification(a: ActorRef, x: CreateNotification): Future[Any] = x match {
    case y: CreateSmsNotificationLike => createSmsNotification(a, y)
    case y: CreateEmailNotificationLike => createMailNotification(a, y)
  }

  def addNotificationN(a: ActorRef, x: Seq[CreateNotification]): Future[Seq[Any]] =
    Future.sequence(x.map(createNotification(a, _)))

  def generateN(generate: => Future[Notification], n: Int): Future[Seq[Notification]] =
    Future.sequence((1 to n).map(_ => generate)).mapTo[Seq[Notification]]

  def getNotifications(a: ActorRef, x: CreateNotification): Future[Map[Notification.Id, Notification]] = x match {
    case y: CreateSmsNotificationLike => listSmsNotifications(a)
    case y: CreateEmailNotificationLike => getMailNotifications(a)
  }

  def notify(a: ActorRef, x: CreateNotification): Future[Any] =
    x match {
      case y: CreateSmsNotificationLike => smsNotify(a)
      case y: CreateEmailNotificationLike => mailNotify(a)
    }

  /**
   * Повторяющиеся тесты для различных типов уведомлений
   */
  def createNotificationBehavior(newActor: => Future[ActorRef], generate: => Future[CreateNotification]): Unit = {
    s"[$generateTestName] create notification" in {
      for {
        a <- newActor
        x <- generate
        _ <- createNotification(a, x)
        y <- getNotifications(a, x)
      } yield y should have size 1
    }
  }

  def notifyBehavior(newActor: => Future[ActorRef], newActorWithFail: => Future[ActorRef], generate: => Future[CreateNotification]): Unit = {
    val name: String = generateTestName

    s"[$name] notify recipients" in {
      for {
        a <- newActor
        n1 <- generate
        n2 <- generate
        _ <- createNotification(a, n1)
        _ <- createNotification(a, n2)
        x <- getNotifications(a, n1)
        _ <- notify(a, n1)
        y <- getNotifications(a, n1)
      } yield {
        x should not be empty
        y shouldBe empty
      }
    }
    s"[$name] update retry" in {
      for {
        a <- newActorWithFail
        n1 <- generate
        n2 <- generate
        _ <- createNotification(a, n1)
        _ <- createNotification(a, n2)
        x <- getNotifications(a, n1)
        _ <- notify(a, n1)
        y <- getNotifications(a, n1)
      } yield {
        y should not be empty
        y.values.map(_.retry) should contain only 1
      }
    }
    s"[$name] delete notifications" in {
      for {
        a <- newActorWithFail
        n <- generate
        _ <- createNotification(a, n)
        x <- getNotifications(a, n)
        _ <- notify(a, n)
        _ <- notify(a, n)
        y <- getNotifications(a, n)
      } yield y shouldBe empty
    }
  }
}
