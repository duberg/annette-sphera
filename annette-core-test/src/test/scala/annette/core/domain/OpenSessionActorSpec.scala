package annette.core.domain

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.tenancy.OpenSessionService
import annette.core.domain.tenancy.OpenSessionService.{ OpenSessionOpt, OpenSessionSeq }
import annette.core.domain.tenancy.model.OpenSessionUpdate
import annette.core.test.PersistenceSpec
import org.joda.time.DateTime

import scala.concurrent.Future

class OpenSessionActorSpec extends TestKit(ActorSystem("OpenSessionActorSpec"))
  with PersistenceSpec with NewOpenSession {
  "A OpenSessionActor" when receive {
    "CreateOpenSessionCmd" must {
      "create new OpenSession" in {
        val s1 = newOpenSession
        val s2 = newOpenSession
        val actor = newOpenSessionActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          c2 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s2))
          r <- ask(actor, OpenSessionService.FindAllOpenSessions).mapTo[OpenSessionService.OpenSessionSeq].map(_.entries)
        } yield {
          c1 shouldBe Done
          c2 shouldBe Done
          r.size shouldBe 2
        }
      }
    }
    "UpdateOpenSessionCmd" must {
      "update tenantId" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          tenantId = Some("EXXO"))
        val actor = newOpenSessionActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.tenantId))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some("EXXO")
        }
      }
      "update languageId" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          languageId = Some("RU"))
        val actor = newOpenSessionActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.languageId))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some("RU")
        }
      }
      "update applicationId" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          applicationId = Some("exxo"))
        val actor = newOpenSessionActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.applicationId))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some("exxo")
        }
      }
      "update rememberMe" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          rememberMe = Some(false))
        val actor = newOpenSessionActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.rememberMe))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some(false)
        }
      }
      "update tenantId, languageId, applicationId" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          tenantId = Some("EXXO"),
          languageId = Some("RU"),
          applicationId = Some("exxo"))
        val actor = newOpenSessionActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt]
            .map(_.maybeEntry.map(x => (x.tenantId, x.languageId, x.applicationId)))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some(("EXXO", "RU", "exxo"))
        }
      }
      "update lastOpTimestamp" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          lastOpTimestamp = Some(DateTime.now))
        val actor = newOpenSessionActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.lastOpTimestamp))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe upd.lastOpTimestamp
        }
      }
    }
    "UpdateOpenSessionCmd with wrong id" must {
      "do nothing" in {
        val upd = OpenSessionUpdate(
          id = UUID.randomUUID(),
          rememberMe = Some(false))
        val actor = newOpenSessionActor()
        for {
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))

        } yield {
          u shouldBe OpenSessionService.EntryNotFound
        }
      }

    }
    "DeleteOpenSessionCmd" must {
      "delete" in {
        val s1 = newOpenSession

        val actor = newOpenSessionActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          r1 <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id)).mapTo[OpenSessionOpt].map(_.maybeEntry)
          d <- ask(actor, OpenSessionService.DeleteOpenSessionCmd(s1.id))
          r2 <- ask(actor, OpenSessionService.FindAllOpenSessions).mapTo[OpenSessionSeq].map(_.entries)

        } yield {
          c1 shouldBe Done
          r1 shouldBe Some(s1)
          d shouldBe Done
          r2.size shouldBe 0
        }
      }

    }
    "DeleteOpenSessionCmd with wrong id" must {
      "do nothing" in {
        val actor = newOpenSessionActor()
        for {
          d <- ask(actor, OpenSessionService.DeleteOpenSessionCmd(UUID.randomUUID()))
        } yield {
          d shouldBe OpenSessionService.EntryNotFound
        }
      }

    }
    "getOpenSessionById" must {
      "close session if expired" in {
        val s1 = newOpenSession
        val s2 = s1.copy(timeout = 1, rememberMe = true, startTimestamp = DateTime.now().minusMinutes(14))
        val actor = newOpenSessionActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s2))
          _ <- Future {
            Thread.sleep(100)
          }
          r1 <- ask(actor, OpenSessionService.FindOpenSessionById(s2.id)).mapTo[OpenSessionOpt].map(_.maybeEntry)
          _ <- Future {
            Thread.sleep(100)
          }

        } yield {
          c1 shouldBe Done
          r1 shouldBe None
        }
      }
    }
  }
}
