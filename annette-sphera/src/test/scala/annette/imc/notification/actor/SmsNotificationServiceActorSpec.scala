package annette.imc.notification.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.core.test.PersistenceSpec

class SmsNotificationServiceActorSpec extends TestKit(ActorSystem("SmsNotificationServiceActorSpec"))
  with PersistenceSpec
  with NotificationServiceBehavior {
  "A SmsNotificationServiceActor" must {
    behave like smsNotificationService()
  }
}