package annette.imc.notification.actor

import akka.actor.ActorSystem
import akka.actor.SupervisorStrategy._
import akka.pattern.ask
import akka.testkit.{ TestActorRef, TestKit }
import annette.core.test.PersistenceSpec

class NotificationServiceActorSpec extends TestKit(ActorSystem("NotificationServiceActorSpec"))
  with PersistenceSpec
  with NewNotificationService
  with NewMailNotificationService
  with NewSmsNotificationService
  with NewSmsVerificationService {
  "A supervisor strategy of the NotificationServiceActor" when receive {
    "ArithmeticException" must {
      "resume actor " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationServiceActor](NotificationServiceActor.props(generateId, p, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new ArithmeticException) shouldBe Resume
      }
    }
    "NullPointerException" must {
      "restart actor " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationServiceActor](NotificationServiceActor.props(generateId, p, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new NullPointerException) shouldBe Restart
      }
    }
    "IllegalArgumentException" must {
      "resume actor " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationServiceActor](NotificationServiceActor.props(generateId, p, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new IllegalArgumentException) shouldBe Resume
      }
    }
    "Exception" must {
      "restart actor " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationServiceActor](NotificationServiceActor.props(generateId, p, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new Exception) shouldBe Resume
      }
    }
  }
  "A NotificationServiceActor" when receive {
    "MailNotificationServiceActor.Command" must {
      "forward it" in {
        for {
          a <- newNotificationService
          x <- generateMailNotificationPassword()
          y <- ask(a, MailNotificationServiceActor.AddNotificationCmd(x))
        } yield y shouldBe MailNotificationServiceActor.Done
      }
    }
    "SmsNotificationServiceActor.Command" must {
      "forward it" in {
        for {
          a <- newNotificationService
          x <- generateSmsNotificationPassword()
          y <- ask(a, SmsNotificationServiceActor.AddNotificationCmd(x))
        } yield y shouldBe SmsNotificationServiceActor.Done
      }
    }
    "SmsVerificationServiceActor.Command" must {
      "forward it" in {
        for {
          a <- newNotificationService
          x <- generateSmsVerificationStatus()
          y <- ask(a, SmsVerificationServiceActor.AddVerificationCmd(x))
        } yield y shouldBe SmsVerificationServiceActor.Done
      }
    }
  }
}