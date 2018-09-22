package annette.imc.notification.actor

import akka.actor.ActorRef
import annette.core.persistence.Persistence.SnapshotInterval
import annette.core.test.PersistenceSpec
import annette.imc.notification.model.{ MailNotification, Notification, SmsNotification }

import scala.concurrent.Future

trait NotificationServiceBehavior extends NewSmsNotificationService
  with NewMailNotificationService { _: PersistenceSpec =>
  def smsNotificationService(): Unit = {
    addNotificationBehavior(newSmsNotificationService(), generateSmsNotificationPassword())
    addNotificationBehavior(newSmsNotificationService(), generateSmsNotificationVerification())
    addNotificationBehavior(newSmsNotificationService(), generateSmsNotificationToExpertise())
    addNotificationBehavior(newSmsNotificationService(), generateSmsNotificationToReview())

    deleteNotificationBehavior(newSmsNotificationService(), generateSmsNotificationPassword())
    deleteNotificationBehavior(newSmsNotificationService(), generateSmsNotificationVerification())
    deleteNotificationBehavior(newSmsNotificationService(), generateSmsNotificationToExpertise())
    deleteNotificationBehavior(newSmsNotificationService(), generateSmsNotificationToReview())

    notifyBehavior(
      service = newStubbedSmsNotificationService(),
      serviceWithFail = newStubbedConnectionRefusedSmsNotificationService(),
      generate = generateSmsNotificationPassword())

    notifyBehavior(
      service = newStubbedSmsNotificationService(),
      serviceWithFail = newStubbedConnectionRefusedSmsNotificationService(),
      generate = generateSmsNotificationVerification())

    notifyBehavior(
      newStubbedSmsNotificationService(),
      serviceWithFail = newStubbedConnectionRefusedSmsNotificationService(),
      generate = generateSmsNotificationToExpertise())

    notifyBehavior(
      service = newStubbedSmsNotificationService(),
      serviceWithFail = newStubbedConnectionRefusedSmsNotificationService(),
      generate = generateSmsNotificationToReview())

    updateRetryBehavior(newSmsNotificationService(), generateSmsNotificationPassword())
    updateRetryBehavior(newSmsNotificationService(), generateSmsNotificationVerification())
    updateRetryBehavior(newSmsNotificationService(), generateSmsNotificationToExpertise())
    updateRetryBehavior(newSmsNotificationService(), generateSmsNotificationToReview())

    recoverBehavior(x => newStubbedConnectionRefusedSmsNotificationService(x), generateSmsNotificationPassword())
    recoverBehavior(x => newStubbedConnectionRefusedSmsNotificationService(x), generateSmsNotificationVerification())
    recoverBehavior(x => newStubbedConnectionRefusedSmsNotificationService(x), generateSmsNotificationToExpertise())
    recoverBehavior(x => newStubbedConnectionRefusedSmsNotificationService(x), generateSmsNotificationToReview())

    recoverFromSnapshotBehavior(x => newStubbedConnectionRefusedSmsNotificationService(x), generateSmsNotificationPassword())
    recoverFromSnapshotBehavior(x => newStubbedConnectionRefusedSmsNotificationService(x), generateSmsNotificationVerification())
    recoverFromSnapshotBehavior(x => newStubbedConnectionRefusedSmsNotificationService(x), generateSmsNotificationToExpertise())
    recoverFromSnapshotBehavior(x => newStubbedConnectionRefusedSmsNotificationService(x), generateSmsNotificationToReview())

    /**
     * In debug mode
     */
    notifyBehavior(
      service = newStubbedSmsNotificationServiceInDebug(),
      serviceWithFail = newStubbedConnectionRefusedSmsNotificationServiceInDebug(),
      generate = generateSmsNotificationPassword())

    notifyBehavior(
      service = newStubbedSmsNotificationServiceInDebug(),
      serviceWithFail = newStubbedConnectionRefusedSmsNotificationServiceInDebug(),
      generate = generateSmsNotificationVerification())

    notifyBehavior(
      newStubbedSmsNotificationServiceInDebug(),
      serviceWithFail = newStubbedConnectionRefusedSmsNotificationServiceInDebug(),
      generate = generateSmsNotificationToExpertise())

    notifyBehavior(
      service = newStubbedSmsNotificationServiceInDebug(),
      serviceWithFail = newStubbedConnectionRefusedSmsNotificationServiceInDebug(),
      generate = generateSmsNotificationToReview())
  }

  def mailNotificationService(): Unit = {
    addNotificationBehavior(newMailNotificationService(), generateMailNotificationPassword())
    addNotificationBehavior(newMailNotificationService(), generateMailNotificationToExpertise())
    addNotificationBehavior(newMailNotificationService(), generateMailNotificationToReview())

    deleteNotificationBehavior(newMailNotificationService(), generateMailNotificationPassword())
    deleteNotificationBehavior(newMailNotificationService(), generateMailNotificationToExpertise())
    deleteNotificationBehavior(newMailNotificationService(), generateMailNotificationToReview())

    notifyBehavior(
      service = newStubbedMailNotificationService(),
      serviceWithFail = newStubbedSocketExceptionMailNotificationService(),
      generate = generateMailNotificationPassword())

    notifyBehavior(
      service = newStubbedMailNotificationService(),
      serviceWithFail = newStubbedSocketExceptionMailNotificationService(),
      generate = generateMailNotificationToExpertise())

    notifyBehavior(
      service = newStubbedMailNotificationService(),
      serviceWithFail = newStubbedSocketExceptionMailNotificationService(),
      generate = generateMailNotificationToReview())

    notifyBehavior(
      service = newStubbedMailNotificationService(),
      serviceWithFail = newStubbedConnectionRefusedMailNotificationService(),
      generate = generateMailNotificationPassword())

    notifyBehavior(
      service = newStubbedMailNotificationService(),
      serviceWithFail = newStubbedConnectionRefusedMailNotificationService(),
      generate = generateMailNotificationToExpertise())

    notifyBehavior(
      service = newStubbedMailNotificationService(),
      serviceWithFail = newStubbedConnectionRefusedMailNotificationService(),
      generate = generateMailNotificationToReview())

    updateRetryBehavior(newMailNotificationService(), generateMailNotificationPassword())
    updateRetryBehavior(newMailNotificationService(), generateMailNotificationToExpertise())
    updateRetryBehavior(newMailNotificationService(), generateMailNotificationToReview())

    recoverBehavior(x => newStubbedSocketExceptionMailNotificationService(x), generateMailNotificationPassword())
    recoverBehavior(x => newStubbedSocketExceptionMailNotificationService(x), generateMailNotificationToExpertise())
    recoverBehavior(x => newStubbedSocketExceptionMailNotificationService(x), generateMailNotificationToReview())

    recoverFromSnapshotBehavior(x => newStubbedSocketExceptionMailNotificationService(x), generateMailNotificationPassword())
    recoverFromSnapshotBehavior(x => newStubbedSocketExceptionMailNotificationService(x), generateMailNotificationToExpertise())
    recoverFromSnapshotBehavior(x => newStubbedSocketExceptionMailNotificationService(x), generateMailNotificationToReview())

    /**
     * In debug mode
     */
    notifyBehavior(
      service = newStubbedMailNotificationService(),
      serviceWithFail = newStubbedSocketExceptionMailNotificationServiceInDebug(),
      generate = generateMailNotificationPassword())

    notifyBehavior(
      service = newStubbedMailNotificationService(),
      serviceWithFail = newStubbedSocketExceptionMailNotificationServiceInDebug(),
      generate = generateMailNotificationToExpertise())

    notifyBehavior(
      service = newStubbedMailNotificationService(),
      serviceWithFail = newStubbedSocketExceptionMailNotificationServiceInDebug(),
      generate = generateMailNotificationToReview())
  }

  def addNotification(service: ActorRef, x: Notification): Future[Any] = x match {
    case y: SmsNotification => addSmsNotification(service, y)
    case y: MailNotification => addMailNotification(service, y)
  }

  def addNotificationN(service: ActorRef, x: Seq[Notification]): Future[Seq[Any]] =
    Future.sequence(x.map(addNotification(service, _)))

  def generateN(generate: => Future[Notification], n: Int): Future[Seq[Notification]] =
    Future.sequence((1 to n).map(_ => generate)).mapTo[Seq[Notification]]

  def getNotifications(service: ActorRef, x: Notification): Future[Map[Notification.Id, Notification]] = x match {
    case y: SmsNotification => getSmsNotifications(service)
    case y: MailNotification => getMailNotifications(service)
  }

  def deleteNotification(service: ActorRef, x: Notification): Future[Any] = x match {
    case y: SmsNotification => deleteSmsNotification(service, y.id)
    case y: MailNotification => deleteMailNotification(service, y.id)
  }

  def notify(service: ActorRef, x: Notification): Future[Any] = x match {
    case y: SmsNotification => smsNotify(service)
    case y: MailNotification => mailNotify(service)
  }

  def updateRetry(service: ActorRef, x: Notification, r: Int): Future[Any] = x match {
    case y: SmsNotification => updateSmsRetry(service, x.id, r)
    case y: MailNotification => updateMailRetry(service, x.id, r)
  }

  /**
   * Повторяющиеся тесты для различных типов уведомлений
   */
  def addNotificationBehavior(service: => Future[ActorRef], generate: => Future[Notification]): Unit = {
    val name: String = generateTestName
    s"$name: addNotificationBehavior" when receive {
      "AddNotificationCmd" must {
        s"$name: add notification" in {
          for {
            a <- service
            x <- generate
            _ <- addNotification(a, x)
            y <- getNotifications(a, x)
          } yield {
            y should have size 1
            y should contain key x.id
          }
        }
      }
    }
  }

  def deleteNotificationBehavior(service: => Future[ActorRef], generate: => Future[Notification]): Unit = {
    val name: String = generateTestName
    s"$name: deleteNotificationBehavior" when receive {
      "DeleteNotificationCmd" must {
        s"$name: delete notification" in {
          for {
            a <- service
            n1 <- generate
            n2 <- generate
            _ <- addNotification(a, n1)
            _ <- addNotification(a, n2)
            x <- deleteNotification(a, n1)
            y <- getNotifications(a, n1)
          } yield {
            y should have size 1
            y should contain key n2.id
          }
        }
      }
    }
  }

  def notifyBehavior(service: => Future[ActorRef], serviceWithFail: => Future[ActorRef], generate: => Future[Notification]): Unit = {
    val name: String = generateTestName
    s"$name: notifyBehavior" when receive {
      "NotifyCmd" must {
        s"$name: notify recipients" in {
          for {
            a <- service
            n1 <- generate
            n2 <- generate
            _ <- addNotification(a, n1)
            _ <- addNotification(a, n2)
            x <- getNotifications(a, n1)
            _ <- notify(a, n1)
            y <- getNotifications(a, n1)
          } yield {
            x should not be empty
            y shouldBe empty
          }
        }
        s"$name: update retry" in {
          for {
            a <- serviceWithFail
            n1 <- generate
            n2 <- generate
            _ <- addNotification(a, n1)
            _ <- addNotification(a, n2)
            x <- getNotifications(a, n1)
            _ <- notify(a, n1)
            y <- getNotifications(a, n1)
          } yield {
            y should not be empty
            y.values.map(_.retry) should contain only 1
          }
        }
        s"$name: delete notifications" in {
          for {
            a <- serviceWithFail
            n <- generate
            _ <- addNotification(a, n)
            x <- getNotifications(a, n)
            _ <- notify(a, n)
            _ <- notify(a, n)
            y <- getNotifications(a, n)
          } yield y shouldBe empty
        }
      }
    }
  }

  def updateRetryBehavior(service: => Future[ActorRef], generate: => Future[Notification]): Unit = {
    val name: String = generateTestName
    s"$name: updateRetryBehavior" when receive {
      "UpdateRetryCmd" must {
        s"$name: update retry" in {
          for {
            a <- service
            n <- generate
            _ <- addNotification(a, n)
            _ <- updateRetry(a, n, 1)
            x <- getNotifications(a, n)
          } yield {
            x should not be empty
            x.head._2.retry shouldBe 1
          }
        }
        s"$name: delete notification" in {
          for {
            a <- service
            n <- generate
            _ <- addNotification(a, n)
            _ <- updateRetry(a, n, 0)
            x <- getNotifications(a, n)
          } yield x shouldBe empty
        }
      }
    }
  }

  def recoverBehavior(serviceWithFail: String => Future[ActorRef], generate: => Future[Notification]): Unit = {
    val name: String = generateTestName
    s"$name: recoverBehavior" when receive {
      "recover" must {
        s"$name: restore all notifications" in {
          for {
            id <- generateIdFuture
            a <- serviceWithFail(id)
            n1 <- generate
            n2 <- generate
            n3 <- generate
            n4 <- generate
            _ <- addNotification(a, n1)
            _ <- addNotification(a, n2)
            _ <- addNotification(a, n3)
            _ <- addNotification(a, n4)
            _ <- kill(a)
            a <- serviceWithFail(id)
            x <- getNotifications(a, n1)
          } yield {
            x should have size 4
            x.values.map(_.retry) should contain only 2
          }
        }
        s"$name: restore all notifications after notify" in {
          for {
            id <- generateIdFuture
            a <- serviceWithFail(id)
            n1 <- generate
            n2 <- generate
            n3 <- generate
            n4 <- generate
            _ <- addNotification(a, n1)
            _ <- addNotification(a, n2)
            _ <- addNotification(a, n3)
            _ <- addNotification(a, n4)
            _ <- notify(a, n1)
            x <- getNotifications(a, n1)
            _ <- kill(a)
            a <- serviceWithFail(id)
            y <- getNotifications(a, n1)
          } yield {
            x should have size 4
            x.values.map(_.retry) should contain only 1
            y should have size 4
            y.values.map(_.retry) should contain only 1
          }
        }
      }
    }
  }

  def recoverFromSnapshotBehavior(serviceWithFail: String => Future[ActorRef], generate: => Future[Notification]): Unit = {
    val name: String = generateTestName
    s"$name: recoverFromSnapshotBehavior" when receive {
      "snapshot" must {
        s"$name: restore all notifications" in {
          val id = generateId
          val n = SnapshotInterval + SnapshotInterval / 2
          for {
            a <- serviceWithFail(id)
            x <- generateN(generate, n)
            _ <- addNotificationN(a, x)
            _ <- notify(a, x.head)
            y <- getNotifications(a, x.head)
            _ <- kill(a)
            a <- serviceWithFail(id)
            z <- getNotifications(a, x.head)
          } yield {
            x should have size n
            y should have size n
            z should have size n
          }
        }
      }
    }
  }
}
