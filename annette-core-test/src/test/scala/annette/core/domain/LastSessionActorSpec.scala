package annette.core.domain

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.tenancy.LastSessionService
import annette.core.domain.tenancy.LastSessionService.LastSessionOpt
import annette.core.domain.tenancy.OpenSessionService.{OpenSessionOpt, OpenSessionSeq}
import annette.core.domain.tenancy.model.OpenSessionUpdate
import annette.core.test.PersistenceSpec
import org.joda.time.DateTime

class LastSessionActorSpec  extends TestKit(ActorSystem("LastSessionActorSpec"))
  with PersistenceSpec with NewLastSession {
  "A LastSessionActor" when receive {
    "StoreLastSessionCmd" must {
      "create new LastSession" in {
        val s1 = newLastSession
        val s2 = newLastSession
        val actor = newLastSessionActor()
        for {
          c1 <- ask(actor, LastSessionService.StoreLastSessionCmd(s1))
          c2 <- ask(actor, LastSessionService.StoreLastSessionCmd(s2))
          r <- ask(actor, LastSessionService.FindAllLastSessions).mapTo[LastSessionService.LastSessionSeq].map(_.entries)
        } yield {
          c1 shouldBe Done
          c2 shouldBe Done
          r.size shouldBe 2
        }
      }
    }
    "StoreLastSessionCmd with the same userId" must {
      "update" in {
        val s1 = newLastSession
        val s2 = newLastSession.copy(userId = s1.userId)
        val actor = newLastSessionActor()
        for {
          c1 <- ask(actor, LastSessionService.StoreLastSessionCmd(s1))
          c2 <- ask(actor, LastSessionService.StoreLastSessionCmd(s2))
          r <- ask(actor, LastSessionService.FindAllLastSessions).mapTo[LastSessionService.LastSessionSeq].map(_.entries)
          s <- ask(actor, LastSessionService.FindLastSessionByUserId(s1.userId)).mapTo[LastSessionOpt].map(_.maybeEntry)
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
