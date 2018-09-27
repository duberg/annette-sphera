package annette.core.notification.actor

import akka.actor.ActorSystem
import akka.actor.SupervisorStrategy._
import akka.testkit.{ TestActorRef, TestKit }
import annette.core.notification.{ NewNotificationManager, SendPasswordToEmailNotification, SendPasswordToPhoneNotification }
import annette.core.test.PersistenceSpec

class NotificationManagerActorSpec extends TestKit(ActorSystem("NotificationManagerActorSpec"))
  with PersistenceSpec
  with NewNotificationManager
  with NewEmailNotificationActor
  with NewSmsNotificationActor {
  "A supervisor strategy of the NotificationManagerActor" when receive {
    "ArithmeticException" must {
      "resume actorRef " in {
        val supervisor = TestActorRef[NotificationManagerActor](NotificationManagerActor.props(generateActorId, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new ArithmeticException) shouldBe Resume
      }
    }
    "NullPointerException" must {
      "restart actorRef " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationManagerActor](NotificationManagerActor.props(generateActorId, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new NullPointerException) shouldBe Restart
      }
    }
    "IllegalArgumentException" must {
      "resume actorRef " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationManagerActor](NotificationManagerActor.props(generateActorId, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new IllegalArgumentException) shouldBe Resume
      }
    }
    "Exception" must {
      "restart actorRef " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationManagerActor](NotificationManagerActor.props(generateActorId, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new Exception) shouldBe Resume
      }
    }
  }
  "A NotificationManagerActor" when receive {
    "MailNotificationManagerActor.Command" must {
      "forward it" in {
        for {
          x1 <- newNotificationManagerActor()
          x2 <- generateCreateSendPasswordToEmailNotification()
          x3 <- ask(x1, EmailNotificationActor.CreateNotificationCmd(x2))
            .mapTo[EmailNotificationActor.CreateNotificationSuccess]
            .map(_.x)
        } yield x3 shouldBe a[SendPasswordToEmailNotification]
      }
    }
    "SmsNotificationManagerActor.Command" must {
      "forward it" in {
        for {
          x1 <- newNotificationManagerActor()
          x2 <- generateCreateSendPasswordToPhoneNotification()
          x3 <- ask(x1, SmsNotificationActor.CreateNotificationCmd(x2))
            .mapTo[SmsNotificationActor.CreateNotificationSuccess]
            .map(_.x)
        } yield x3 shouldBe a[SendPasswordToPhoneNotification]
      }
    }
  }
}