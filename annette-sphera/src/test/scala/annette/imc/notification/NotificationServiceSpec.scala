package annette.imc.notification

import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.core.test.PersistenceSpec
import annette.imc.notification.actor.{ NewNotificationService, NewSmsVerificationService }

class NotificationServiceSpec extends TestKit(ActorSystem("NotificationServiceSpec"))
  with PersistenceSpec
  with NewNotificationService
  with NewSmsVerificationService {
  "A NotificationServiceSpec" when {
    "addSmsVerification" must {
      "add notification and send ApsActor.UpdateBulletinCmd" in {
        for {
          (p, s) <- newNotificationServiceWrapper
          v <- generateSmsVerificationVoted()
          x <- s.addSmsVerificationVoted(v.phone, v.apId, v.bulletin, "RU")
          y <- s.getSmsNotifications
          z <- s.smsVerify(x.id, x.code)
          q <- s.getSmsVerifications
        } yield {
          1000 to 9999 contains x.code.toInt shouldBe true
          y.map(_.id) should contain(x.id)
          z shouldBe "Done"
          expectUpdateBulletinCmd(p)
          q shouldBe empty
        }
      }
    }
  }
}