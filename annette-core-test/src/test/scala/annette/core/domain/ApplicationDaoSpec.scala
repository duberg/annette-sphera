package annette.core.domain

import java.util.UUID

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.TestKit
import annette.core.domain.application._
import annette.core.security.verification.VerificationBus
import annette.core.test.PersistenceSpec
import com.typesafe.config.{ Config, ConfigFactory }

class ApplicationDaoSpec extends TestKit(ActorSystem("ApplicationDaoSpec"))
  with PersistenceSpec
  with NewApplication {
  lazy val config: Config = ConfigFactory.load()

  private def newCoreServiceActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(CoreService.props(
      config = config,
      verificationBus = new VerificationBus), s"CoreService-$uuid")
  }

  def newApplicationDao(): ApplicationService = {
    val coreServiceActor = newCoreServiceActor()
    new ApplicationService(coreServiceActor)
  }

  "An ApplicationDao" when {
    "createUser" must {
      "createUser new application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val dao = newApplicationDao()
        for {
          cc1 <- dao.create(c1)
          _ <- dao.create(c2)
          ccs <- dao.selectAll
        } yield ccs.size shouldBe 2
      }
      "should not createUser new application if it already exists" in {
        val c1 = Application("App1", "app1", "APP1")
        val dao = newApplicationDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- recoverToExceptionIf[ApplicationAlreadyExists] { dao.create(c1) }
          ccs <- dao.selectAll

        } yield {
          cc2.exceptionMessage.get("code") shouldBe Some("core.application.alreadyExists")
          ccs.size shouldBe 1
        }
      }
    }

    "updateUser" must {
      "updateUser application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP1")
        val dao = newApplicationDao()
        for {

          cc1 <- dao.create(c1)
          cc2 <- dao.update(UpdateApplication(Some(c2.name), Some(c2.code), c1.id))
          ccs <- dao.getApplicationById(c1.id)
        } yield ccs shouldBe Some(c2)
      }
      "should not updateUser application if it doesn't exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val dao = newApplicationDao()
        for {
          cc1 <- recoverToExceptionIf[ApplicationNotFound](dao.update(UpdateApplication(Some(c2.name), Some(c2.code), c1.id)))
        } yield {
          cc1.exceptionMessage.get("code") shouldBe Some("core.application.notFound")
        }
      }
    }

    "delete" must {
      "deleteUser application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val dao = newApplicationDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- dao.create(c2)
          ccs <- dao.selectAll
          d1 <- dao.delete(c1.id)
          ccr <- dao.selectAll
        } yield {
          ccs.head shouldBe c1
          ccs.last shouldBe c2
          d1 shouldBe ()
          ccr.head shouldBe c2
          ccr.size shouldBe 1
        }
      }
      "should not deleteUser application if it does not exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val dao = newApplicationDao()
        for {
          d1 <- recoverToExceptionIf[ApplicationNotFound](dao.delete(c1.id))
        } yield {
          d1.exceptionMessage.get("code") shouldBe Some("core.application.notFound")
        }
      }
    }
  }
}