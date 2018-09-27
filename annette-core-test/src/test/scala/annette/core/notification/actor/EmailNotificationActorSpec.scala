package annette.core.notification.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.core.test.PersistenceSpec

class EmailNotificationActorSpec extends TestKit(ActorSystem("MailNotificationActorSpec"))
  with PersistenceSpec
  with NotificationActorBehavior {
  behave like mailNotificationActor()
}