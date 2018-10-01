//package annette.imc.notification.actor
//
//import akka.actor.ActorSystem
//import akka.testkit.TestKit
//import annette.core.test.PersistenceSpec
//
//class MailNotificationServiceActorSpec extends TestKit(ActorSystem("MailNotificationServiceActorSpec"))
//  with PersistenceSpec
//  with NotificationServiceBehavior {
//  "A MailNotificationServiceActor" must {
//    behave like mailNotificationService()
//  }
//}