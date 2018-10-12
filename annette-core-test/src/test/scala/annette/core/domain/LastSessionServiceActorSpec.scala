package annette.core.domain

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.tenancy.LastSessionManager
import annette.core.domain.tenancy.LastSessionManager.LastSessionOpt
import annette.core.domain.tenancy.OpenSessionManager.{ OpenSessionOpt, OpenSessionSeq }
import annette.core.domain.tenancy.model.OpenSessionUpdate
import annette.core.test.PersistenceSpec
import org.joda.time.DateTime

class LastSessionServiceActorSpec extends TestKit(ActorSystem("LastSessionActorSpec"))
  with PersistenceSpec with NewLastSession {
  "A LastSessionActor" when receive {
    "StoreLastSessionCmd" must {
      "createUser new LastSession" in {
        val s1 = newLastSession
        val s2 = newLastSession
        val actor = newLastSessionActor()
        for {
          c1 <- ask(actor, LastSessionManager.StoreLastSessionCmd(s1))
          c2 <- ask(actor, LastSessionManager.StoreLastSessionCmd(s2))
          r <- ask(actor, LastSessionManager.FindAllLastSessions).mapTo[LastSessionManager.LastSessionSeq].map(_.entries)
        } yield {
          c1 shouldBe Done
          c2 shouldBe Done
          r.size shouldBe 2
        }
      }
    }
    "StoreLastSessionCmd with the same userId" must {
      "updateUser" in {
        val s1 = newLastSession
        val s2 = newLastSession.copy(userId = s1.userId)
        val actor = newLastSessionActor()
        for {
          c1 <- ask(actor, LastSessionManager.StoreLastSessionCmd(s1))
          c2 <- ask(actor, LastSessionManager.StoreLastSessionCmd(s2))
          r <- ask(actor, LastSessionManager.FindAllLastSessions).mapTo[LastSessionManager.LastSessionSeq].map(_.entries)
          s <- ask(actor, LastSessionManager.FindLastSessionByUserId(s1.userId)).mapTo[LastSessionOpt].map(_.maybeEntry)
        } yield {
          c1 shouldBe Done
          c2 shouldBe Done
          r.size shouldBe 1
          s shouldBe Some(s2)
        }
      }
    }
  }
}
