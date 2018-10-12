package annette.core.security.verification

import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.core.security.verification.Verification._
import annette.core.test.PersistenceSpec

class VerificationActorSpec extends TestKit(ActorSystem("VerificationActorSpec"))
  with PersistenceSpec
  with NewVerificationActor {

  "A VerificationActor" when receive {
    "CreateVerificationCmd" must {
      "createUser verification" in {
        for {
          a <- newVerificationActor()
          x1 <- createVerification(a)
          x2 <- listVerifications(a)
        } yield {
          x2 should have size 1
          x2 should contain key x1.id
        }
      }
    }
    "DeleteNotificationCmd" must {
      "deleteUser verification" in {
        for {
          a <- newVerificationActor()
          x1 <- createVerification(a)
          x2 <- createVerification(a)
          x3 <- deleteVerification(a, x1.id)
          x4 <- listVerifications(a)
        } yield {
          x4 should have size 1
          x4 should contain key x2.id
        }
      }
    }
    "VerifyCmd" must {
      "verify and deleteUser verification" in {
        for {
          a <- newVerificationActor()
          x1 <- createVerification(a)
          x2 <- createVerification(a)
          x3 <- verify(a, x1.id, x1.code)
          x4 <- listVerifications(a)
        } yield x4.keys should contain only x2.id
      }
      "return InvalidCode" in {
        for {
          a <- newVerificationActor()
          x1 <- createVerification(a)
          x2 <- verify(a, x1.id, generateString())
        } yield x2 shouldBe InvalidCode
      }
    }
    "recover" must {
      "restore all verifications" in {
        val id = generateString()
        for {
          a <- newVerificationActor(id)
          _ <- createVerification(a)
          _ <- createVerification(a)
          _ <- kill(a)
          a <- newVerificationActor(id)
          x1 <- listVerifications(a)
        } yield x1 should have size 2
      }
      "restore all verifications after verify" in {
        val id = generateString()
        for {
          a <- newVerificationActor(id)
          x1 <- createVerification(a)
          x2 <- createVerification(a)
          _ <- verify(a, x1.id, x1.code)
          x3 <- listVerifications(a)
          _ <- kill(a)
          a <- newVerificationActor(id)
          x4 <- listVerifications(a)
        } yield {
          x3 should have size 1
          x4 should have size 1
        }
      }
    }
  }
}