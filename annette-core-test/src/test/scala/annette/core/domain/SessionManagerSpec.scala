package annette.core.domain

import java.util.UUID

import akka.Done
import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.AnnetteException
import annette.core.domain.application.ApplicationManager
import annette.core.domain.application._
import annette.core.domain.language.LanguageService
import annette.core.domain.language.model.{ Language, LanguageUpdate }
import annette.core.domain.tenancy.{ SessionManager, _ }
import annette.core.domain.tenancy.model._
import annette.core.security.verification.VerificationBus
import annette.core.test.PersistenceSpec
import com.typesafe.config.{ Config, ConfigFactory }
import org.joda.time.DateTime

import scala.concurrent.Future

class SessionManagerSpec extends TestKit(ActorSystem("SessionDaoSpec"))
  with PersistenceSpec
  with NewOpenSession {
  lazy val config: Config = ConfigFactory.load()

  def newCoreServiceActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(CoreService.props(
      config = config,
      verificationBus = new VerificationBus), s"CoreService-$uuid")
  }

  def newSessionDao(): SessionManager = {
    val coreServiceActor = newCoreServiceActor()
    new SessionManager(coreServiceActor, system)
  }

  "A SessionDao" when {
    "createSession" must {
      "create new openSession" in {
        val s1 = newOpenSession
        val s2 = newOpenSession
        val dao = newSessionDao()
        for {
          cc1 <- dao.createSession(s1)
          r1 <- dao.getOpenSessionById(s1.id)
          cc2 <- dao.createSession(s2)
          r2 <- dao.getAllOpenSessions

        } yield {
          cc1 shouldBe s1
          r1 shouldBe Some(s1)
          cc2 shouldBe s2
          r2.size shouldBe 2
        }
      }

      "throw an exception if the session already exists" in {
        val s1 = newOpenSession
        val s2 = newOpenSession.copy(id = s1.id)
        val dao = newSessionDao()
        for {
          cc1 <- dao.createSession(s1)
          r1 <- dao.getOpenSessionById(s1.id)
          cc2 <- recoverToExceptionIf[AnnetteException](dao.createSession(s2))
          r2 <- dao.getAllOpenSessions

        } yield {
          cc1 shouldBe s1
          r1 shouldBe Some(s1)
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.session.alreadyExists")
          r2.size shouldBe 1
        }
      }
    }

    "closeSession" must {
      """
        |1. delete open session
        |2. update/create last session
        |3. create session history
      """.stripMargin in {
        val s1 = newOpenSession
        val dao = newSessionDao()
        for {
          cc1 <- dao.createSession(s1)
          r1 <- dao.getOpenSessionById(s1.id)
          r2 <- dao.getAllLastSessions
          r3 <- dao.getAllSessionHistories
          d <- dao.closeSession(s1.id)
          r4 <- dao.getAllOpenSessions
          _ <- Future { Thread.sleep(100) }
          r5 <- dao.getAllLastSessions
          r6 <- dao.getAllSessionHistories

        } yield {
          cc1 shouldBe s1
          r1 shouldBe Some(s1)
          d shouldBe ()
          r2.size shouldBe 0
          r3.size shouldBe 0
          r4.size shouldBe 0
          r5.size shouldBe 1
          r6.size shouldBe 1
        }
      }
    }
    "throw an exception if the session doesn't exist" in {
      val dao = newSessionDao()
      for {
        d <- recoverToExceptionIf[AnnetteException](dao.closeSession(UUID.randomUUID()))
        r2 <- dao.getAllOpenSessions

      } yield {
        d.exceptionMessage.get("code") shouldBe Some("core.tenancy.session.notFound")
        r2.size shouldBe 0
      }
    }
  }
  "updateLastOpTimestamp" must {
    "update the field 'lastOpTimestamp'" in {
      val s1 = newOpenSession
      val ts = s1.lastOpTimestamp
      val dao = newSessionDao()
      for {
        cc1 <- dao.createSession(s1)
        u <- Future.successful(dao.updateLastOpTimestamp(s1.id))
        _ <- Future { Thread.sleep(100) }
        r1 <- dao.getOpenSessionById(s1.id)

      } yield {
        cc1 shouldBe s1
        u shouldBe ()
        r1 should not be ts
      }
    }
  }
  "updateTenantApplicationLanguage" must {
    "update tenantId, languageId, applicationId" in {
      val s1 = newOpenSession
      val upd = OpenSessionUpdate(
        id = s1.id,
        tenantId = Some("EXXO"),
        languageId = Some("RU"),
        applicationId = Some("exxo"))
      val dao = newSessionDao()
      for {
        c1 <- dao.createSession(s1)
        u <- Future.successful(dao.updateTenantApplicationLanguage(s1.id, "EXXO", "exxo", "RU"))
        r <- dao.getOpenSessionById(s1.id)
      } yield {
        c1 shouldBe s1
        u shouldBe ()
        r.map(x => (x.tenantId, x.applicationId, x.languageId)) shouldBe Some(("EXXO", "exxo", "RU"))
      }
    }
  }
  "getOpenSessionById" must {
    "find open session if exists" in {
      val s1 = newOpenSession
      val dao = newSessionDao()
      for {
        cc1 <- dao.createSession(s1)
        r1 <- dao.getOpenSessionById(s1.id)

      } yield {
        cc1 shouldBe s1
        r1 shouldBe Some(s1)
      }
    }

    "return None if doesn't exist" in {
      val dao = newSessionDao()
      val id = UUID.randomUUID()
      for {
        r1 <- dao.getOpenSessionById(id)

      } yield {
        r1 shouldBe None
      }
    }
    "getOpenSessionById" must {
      "close session if expired" in {
        val s1 = newOpenSession
        val s2 = s1.copy(timeout = 1, rememberMe = true, startTimestamp = DateTime.now().minusMinutes(14))
        val dao = newSessionDao()
        for {
          cc1 <- dao.createSession(s2)
          _ <- Future {
            Thread.sleep(500)
          }
          r1 <- dao.getOpenSessionById(s1.id)
          _ <- Future {
            Thread.sleep(500)
          }
          r2 <- dao.getAllLastSessions
          r4 <- dao.getAllOpenSessions
          r3 <- dao.getAllSessionHistories
          r5 <- dao.getLastSessionByUserId(s2.userId)
          r6 <- dao.getSessionHistoryById(s2.id)

        } yield {
          cc1 shouldBe s2
          r1 shouldBe None
          r2.size shouldBe 1
          r3.size shouldBe 1
          r4.size shouldBe 0
          r5.map(_.tenantId) shouldBe Some(s2.tenantId)
          r6.map(_.tenantId) shouldBe Some(s2.tenantId)
        }
      }
    }
  }
}
