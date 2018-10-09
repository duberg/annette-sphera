
package annette.core.domain

import java.util.UUID

import akka.Done
import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.application.ApplicationManager
import annette.core.domain.application._
import annette.core.domain.language.{ LanguageAlreadyExists, LanguageManager, LanguageNotFound, LanguageService }
import annette.core.domain.language.model.{ Language, LanguageUpdate }
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model._
import annette.core.security.verification.VerificationBus
import annette.core.test.PersistenceSpec
import com.typesafe.config.{ Config, ConfigFactory }

class LanguageManagerSpec extends TestKit(ActorSystem("UserActorSpec"))
  with PersistenceSpec
  with NewApplication
  with NewLanguage
  with NewUser {
  lazy val config: Config = ConfigFactory.load()

  def newCoreServiceActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(CoreService.props(
      config = config,
      verificationBus = new VerificationBus), s"CoreService-$uuid")
  }

  def newLanguageDao(): LanguageManager = {
    val coreServiceActor = newCoreServiceActor()
    new LanguageManager(coreServiceActor)
  }

  "The LanguageDao" when {
    "create" must {
      "create new language" in {
        val c1 = Language("English", "EN")
        val c2 = Language("Russian", "RU")
        val dao = newLanguageDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- dao.create(c2)
          ccs <- dao.selectAll
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          ccs.head shouldBe c1
          ccs.last shouldBe c2
        }
      }
      "should not create new language if it already exists" in {
        val c1 = Language("English", "EN")
        val dao = newLanguageDao()
        for {

          cc1 <- dao.create(c1)
          cc2 <- recoverToExceptionIf[LanguageAlreadyExists](dao.create(c1))
        } yield {
          cc1 shouldBe ()
          cc2.exceptionMessage.get("code") shouldBe Some("core.language.alreadyExists")
        }
      }
    }

    "update" must {
      "update language" in {
        val c1 = Language("English", "EN")
        val c2 = Language("English-US", "EN")
        val dao = newLanguageDao()
        for {

          cc1 <- dao.create(c1)
          cc2 <- dao.update(LanguageUpdate(Some(c2.name), c1.id))
          ccs <- dao.getLanguageById(c1.id)
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          ccs shouldBe Some(c2)
        }
      }
      "should not update language if it doesn't exist" in {
        val c1 = Language("English", "EN")
        val c2 = Language("English-US", "EN")
        val dao = newLanguageDao()
        for {
          cc1 <- recoverToExceptionIf[LanguageNotFound](dao.update(LanguageUpdate(Some(c2.name), c1.id)))
        } yield {
          cc1.exceptionMessage.get("code") shouldBe Some("core.language.notFound")
        }
      }
    }

    "DeleteLanguageCmd" must {
      "delete language" in {
        val c1 = Language("English", "EN")
        val c2 = Language("Russian", "RU")
        val dao = newLanguageDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- dao.create(c2)
          ccs <- dao.selectAll
          d1 <- dao.delete(c1.id)
          ccr <- dao.selectAll
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          ccs.head shouldBe c1
          ccs.last shouldBe c2
          d1 shouldBe ()
          ccr.head shouldBe c2
          ccr.size shouldBe 1
        }
      }
      "should not delete language if it does not exist" in {
        val c1 = Language("English", "EN")
        val dao = newLanguageDao()
        for {
          d1 <- recoverToExceptionIf[LanguageNotFound](dao.delete(c1.id))
        } yield {
          d1.exceptionMessage.get("code") shouldBe Some("core.language.notFound")
        }
      }
    }

  }
}