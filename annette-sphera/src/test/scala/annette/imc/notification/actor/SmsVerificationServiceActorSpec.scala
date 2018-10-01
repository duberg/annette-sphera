//package annette.imc.notification.actor
//
//import akka.actor.ActorSystem
//import akka.testkit.TestKit
//import annette.core.test.PersistenceSpec
//import annette.imc.notification.actor.SmsVerificationServiceActor._
//
//class SmsVerificationServiceActorSpec extends TestKit(ActorSystem("SmsVerificationServiceActorSpec"))
//  with PersistenceSpec
//  with NewSmsVerificationService {
//  "A SmsVerificationServiceActor" when receive {
//    "AddVerificationCmd" must {
//      "add verification SmsVerification.Status" in {
//        for {
//          (p1, p2, a) <- newSmsVerificationService()
//          x <- generateSmsVerificationStatus()
//          y <- addSmsVerification(a, x)
//          z <- getSmsVerifications(a)
//        } yield {
//          y shouldBe Done
//          z should have size 1
//          z should contain key x.id
//        }
//      }
//      "add verification SmsVerification.Voted" in {
//        for {
//          (p1, p2, a) <- newSmsVerificationService()
//          x <- generateSmsVerificationVoted()
//          y <- addSmsVerification(a, x)
//          z <- getSmsVerifications(a)
//        } yield {
//          y shouldBe Done
//          z should have size 1
//          z should contain key x.id
//        }
//      }
//      "send AddNotificationCmd" in {
//        for {
//          (_, p, a) <- newSmsVerificationService()
//          x <- generateSmsVerificationStatus()
//          _ <- addSmsVerification(a, x)
//          _ <- getSmsVerifications(a)
//        } yield expectAddNotificationCmd(p, x)
//      }
//    }
//    "DeleteNotificationCmd" must {
//      "delete notification" in {
//        for {
//          (_, _, a) <- newSmsVerificationService()
//          v1 <- generateSmsVerificationStatus()
//          v2 <- generateSmsVerificationVoted()
//          x <- addSmsVerification(a, v1)
//          y <- addSmsVerification(a, v2)
//          z <- deleteSmsVerification(a, v1.id)
//          q <- getSmsVerifications(a)
//        } yield {
//          x shouldBe Done
//          y shouldBe Done
//          q should have size 1
//          q should contain key v2.id
//        }
//      }
//    }
//    "VerifyCmd" must {
//      "verify and delete verification" in {
//        for {
//          (_, _, a) <- newSmsVerificationService()
//          v1 <- generateSmsVerificationStatus()
//          v2 <- generateSmsVerificationStatus()
//          x <- addSmsVerification(a, v1)
//          y <- addSmsVerification(a, v2)
//          z <- smsVerify(a, v1.id, v1.code)
//          q <- getSmsVerifications(a)
//        } yield {
//          x shouldBe Done
//          y shouldBe Done
//          z shouldBe Done
//          q.keys should contain only v2.id
//        }
//      }
//      "send ChangeStatusCmd" in {
//        for {
//          (p, _, a) <- newSmsVerificationService()
//          v <- generateSmsVerificationStatus()
//          x <- addSmsVerification(a, v)
//          y <- smsVerify(a, v.id, v.code)
//        } yield {
//          x shouldBe Done
//          y shouldBe Done
//          //expectChangeStatusCmd(p)
//        }
//      }
//      "return InvalidCode" in {
//        for {
//          (p, _, a) <- newSmsVerificationService()
//          v <- generateSmsVerificationStatus()
//          x <- addSmsVerification(a, v)
//          y <- smsVerify(a, v.id, generateString())
//        } yield {
//          x shouldBe Done
//          y shouldBe InvalidCode
//        }
//      }
//    }
//    "recover" must {
//      "restore all verifications" in {
//        val id = generateId
//        for {
//          (_, _, a) <- newSmsVerificationService(id)
//          v1 <- generateSmsVerificationStatus()
//          v2 <- generateSmsVerificationVoted()
//          _ <- addSmsVerification(a, v1)
//          _ <- addSmsVerification(a, v2)
//          _ <- kill(a)
//          (_, _, a) <- newSmsVerificationService(id)
//          x <- getSmsVerifications(a)
//        } yield x should have size 2
//      }
//      "restore all verifications after verify" in {
//        val id = generateId
//        for {
//          (_, _, a) <- newSmsVerificationService(id)
//          v1 <- generateSmsVerificationStatus()
//          v2 <- generateSmsVerificationVoted()
//          _ <- addSmsVerification(a, v1)
//          _ <- addSmsVerification(a, v2)
//          _ <- smsVerify(a, v1.id, v1.code)
//          x <- getSmsVerifications(a)
//          _ <- kill(a)
//          (_, _, a) <- newSmsVerificationService(id)
//          y <- getSmsVerifications(a)
//        } yield {
//          x should have size 1
//          y should have size 1
//        }
//      }
//    }
//  }
//}