package annette.core.domain

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.tenancy.SessionHistoryService
import annette.core.domain.tenancy.LastSessionService.LastSessionOpt
import annette.core.domain.tenancy.OpenSessionService.{ OpenSessionOpt, OpenSessionSeq }
import annette.core.domain.tenancy.model.OpenSessionUpdate
import annette.core.test.PersistenceSpec
import org.joda.time.DateTime

class SessionHistoryActorSpec extends TestKit(ActorSystem("SessionHistoryActorSpec"))
  with PersistenceSpec with NewSessionHistory {
  "A SessionHistoryActor" when receive {
    "CreateSessionHistoryCmd" must {
      "create new SessionHistory" in {
        val s1 = newSessionHistory
        val s2 = newSessionHistory
        val actor = newSessionHistoryActor()
        for {
          c1 <- ask(actor, SessionHistoryService.CreateSessionHistoryCmd(s1))
          c2 <- ask(actor, SessionHistoryService.CreateSessionHistoryCmd(s2))
          r <- ask(actor, SessionHistoryService.FindAllSessionHistory)
            .mapTo[SessionHistoryService.SessionHistorySeq].map(_.entries)
        } yield {
          c1 shouldBe Done
          c2 shouldBe Done
          r.size shouldBe 2
        }
      }
    }
    "CreateSessionHistoryCmd with the same id" must {
      "do nothing" in {
        val s1 = newSessionHistory
        val s2 = newSessionHistory.copy(id = s1.id)
        val actor = newSessionHistoryActor()
        for {
          c1 <- ask(actor, SessionHistoryService.CreateSessionHistoryCmd(s1))
          c2 <- ask(actor, SessionHistoryService.CreateSessionHistoryCmd(s2))
          r <- ask(actor, SessionHistoryService.FindAllSessionHistory).mapTo[SessionHistoryService.SessionHistorySeq].map(_.entries)
          r1 <- ask(actor, SessionHistoryService.FindSessionHistoryById(s1.id))
            .mapTo[SessionHistoryService.SessionHistoryOpt].map(_.maybeEntry)
        } yield {
          c1 shouldBe Done
          c2 shouldBe SessionHistoryService.EntryAlreadyExists
          r.size shouldBe 1
          r1 shouldBe Some(s1)
        }
      }
    }
  }
}
