package annette.core.notification.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.core.test.PersistenceSpec

class SmsNotificationActorSpec extends TestKit(ActorSystem("SmsNotificationActorSpec"))
  with PersistenceSpec
  with NotificationActorBehavior {
  behave like smsNotificationActor()
}