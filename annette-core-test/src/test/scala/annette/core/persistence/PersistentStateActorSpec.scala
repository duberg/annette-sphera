package annette.core.persistence

import akka.actor.{ ActorSystem, PoisonPill }
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.test.PersistenceSpec

import scala.concurrent.Future

class PersistentStateActorSpec extends TestKit(ActorSystem("PersistentStateActorSpec"))
  with PersistenceSpec
  with NewPersistentStateActor {
  import Persistence._
  import TestPersistentActor._
  "A PersistentStateActor" when receive {
    "ping" must response {
      "pong" in {
        for {
          a <- newTestPersistentActor()
          x <- ask(a, "ping")
        } yield x shouldBe "pong"
      }
    }
    "Pong" must response {
      "Pong" in {
        for {
          a <- newTestPersistentActor()
          x <- ask(a, Ping)
        } yield x shouldBe Pong
      }
    }
    "kill" must response {
      "terminated" in {
        for {
          a <- newTestPersistentActor()
          x <- ask(a, "kill")
        } yield x shouldBe "terminated"
      }
    }
    "Kill" must response {
      "Terminated" in {
        for {
          a <- newTestPersistentActor()
          x <- ask(a, Kill)
        } yield x shouldBe Terminated
      }
    }
    "event" must response {
      "Persisted" in {
        for {
          a <- newTestPersistentActor()
          x <- ask(a, CreatedEvt("created-evt"))
        } yield x shouldBe Persisted
      }
    }
    "GetAll" must {
      "return all elements from init state" in {
        val s = Seq(generateString(), generateString())
        for {
          a <- newTestPersistentActor(TestPersistentStateActor(s))
          x <- getAll(a)
        } yield x shouldBe s
      }
    }
    "CreateCmd" must {
      "change state" in {
        val ss = Seq(generateString(), generateString())
        val s1 = generateString()
        for {
          a <- newTestPersistentActor(TestPersistentStateActor(ss))
          x <- ask(a, CreateCmd(s1))
          y <- getAll(a)
        } yield {
          x shouldBe Done
          y should contain(s1)
        }
      }
    }
    "AddRelatedCmd" must {
      "add related" in {
        val id = generateUUID
        val p = newTestProbeRef
        for {
          a <- newTestPersistentActor()
          x <- addRelated(a, id, p)
          y <- getRelated(a)
        } yield {
          x shouldBe Success
          y should contain key id
        }
      }
    }
    "RemoveRelatedByActorRefCmd" must {
      "remove related" in {
        val id = generateUUID
        val p = newTestProbeRef
        for {
          a <- newTestPersistentActor()
          x <- addRelated(a, id, p)
          y <- removeRelated(a, p)
          z <- getRelated(a)
        } yield {
          x shouldBe Success
          y shouldBe Success
          z shouldBe empty
        }
      }
    }
    "create snapshot" in {
      val n = SnapshotInterval + SnapshotInterval / 2
      for {
        a <- newTestPersistentActor()
        _ <- Future.sequence {
          for (i <- 1 to n) yield ask(a, CreateCmd(generateString()))
        }
        x <- getAll(a)
        z <- ask(a, HasSnapshot)
      } yield {
        x should have size n
        z shouldBe Yes
      }
    }
    "snapshot" must {
      "restore" in {
        val id = s"TestRecovering-$generateId"
        val n = SnapshotInterval + SnapshotInterval / 2
        for {
          a <- newTestPersistentActor(id = id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(id = id)
          z <- ask(c, GetAll).mapTo[MultipleEntries].map(_.entries)
        } yield {
          x should have size n
          y shouldBe Yes
          z should have size n
        }
      }
      "restore initState" in {
        val id = s"TestRecovering-$generateId"
        val n = SnapshotInterval + SnapshotInterval / 2
        val s = TestPersistentStateActor(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(s, id)
          z <- ask(c, GetAll).mapTo[MultipleEntries].map(_.entries)
        } yield {
          x should have size n + s.size
          y shouldBe Yes
          z should have size n + s.size
        }
      }
    }
    "recover" must {
      "recover initState" in {
        val id = s"TestRecovering-$generateId"
        val n = SnapshotInterval / 2
        val s = TestPersistentStateActor(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(s, id)
          z <- ask(c, GetAll).mapTo[MultipleEntries].map(_.entries)
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
        }
      }
      "recover when created with initState empty" in {
        val id = s"TestRecovering-$generateId"
        val n = SnapshotInterval / 2
        val s = TestPersistentStateActor(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(id = id) // empty initState
          z <- ask(c, GetAll).mapTo[MultipleEntries].map(_.entries)
          i <- ask(c, GetRecoveredState).mapTo[Option[TestPersistentStateActor]]
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
          i should not be empty
          i.get should have size n + s.size
        }
      }
      "afterRecover" in {
        val id = s"TestRecovering-$generateId"
        val n = SnapshotInterval / 2
        val s = TestPersistentStateActor(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(s, id)
          z <- ask(c, GetAll).mapTo[MultipleEntries].map(_.entries)
          i <- ask(c, GetRecoveredState).mapTo[Option[TestPersistentStateActor]]
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
          i should not be empty
          i.get should have size n + s.size
        }
      }
    }
    /**
     * Tests: Create Snapshot only for [[PersistentCommand]]
     */
    "CQRSCommand" must {
      "not create snapshot" in {
        val id = generateId
        val n = SnapshotInterval + SnapshotInterval / 2
        val s = TestPersistentStateActor(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, GetAll)
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(s, id)
          z <- ask(c, GetAll).mapTo[MultipleEntries].map(_.entries)
        } yield {
          x should have size s.size
          y shouldBe No
          z should have size s.size
        }
      }
    }
    // todo: add probe test
    //    "Unknown command" must response {
    //      "UnknownCommand" in {
    //        for {
    //          a <- newTestPersistentActor()
    //          x <- ask(a, "unknown")
    //        } yield x shouldBe UnknownCommand
    //      }
    //    }
  }
}