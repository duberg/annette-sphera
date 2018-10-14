package annette.core.domain

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.core.domain.tenancy.SessionHistoryManager
import annette.core.test.PersistenceSpec

class SessionHistoryManagerActorSpec extends TestKit(ActorSystem("SessionHistoryActorSpec"))
  with PersistenceSpec with NewSessionHistory {
  "A SessionHistoryActor" when receive {
    "CreateSessionHistoryCmd" must {
      "createUser new SessionHistory" in {
        val s1 = newSessionHistory
        val s2 = newSessionHistory
        val actor = newSessionHistoryActor()
        for {
          c1 <- ask(actor, SessionHistoryManager.CreateSessionHistoryCmd(s1))
          c2 <- ask(actor, SessionHistoryManager.CreateSessionHistoryCmd(s2))
          r <- ask(actor, SessionHistoryManager.FindAllSessionHistory)
            .mapTo[SessionHistoryManager.SessionHistorySeq].map(_.entries)
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
          c1 <- ask(actor, SessionHistoryManager.CreateSessionHistoryCmd(s1))
          c2 <- ask(actor, SessionHistoryManager.CreateSessionHistoryCmd(s2))
          r <- ask(actor, SessionHistoryManager.FindAllSessionHistory).mapTo[SessionHistoryManager.SessionHistorySeq].map(_.entries)
          r1 <- ask(actor, SessionHistoryManager.FindSessionHistoryById(s1.id))
            .mapTo[SessionHistoryManager.SessionHistoryOpt].map(_.maybeEntry)
        } yield {
          c1 shouldBe Done
          c2 shouldBe SessionHistoryManager.EntryAlreadyExists
          r.size shouldBe 1
          r1 shouldBe Some(s1)
        }
      }
    }
  }
}
