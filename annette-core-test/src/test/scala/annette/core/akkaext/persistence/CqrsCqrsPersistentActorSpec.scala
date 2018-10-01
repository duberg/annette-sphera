package annette.core.akkaext.persistence

import akka.actor.{ ActorSystem, PoisonPill }
import akka.testkit.TestKit
import annette.core.akkaext.persistence.TestCqrsPersistentActor._
import annette.core.akkaext.actor.ActorId
import annette.core.akkaext.actor.CqrsQuery._
import annette.core.akkaext.actor.CqrsResponse._
import annette.core.akkaext.persistence.CqrsPersistentActor._
import annette.core.test.PersistenceSpec

import scala.concurrent.Future

class CqrsPersistentActorSpec extends TestKit(ActorSystem("CqrsPersistentActorSpec"))
  with PersistenceSpec
  with NewCqrsPersistentActor {
  "A CqrsPersistentActorLike" when receive {
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
          a <- newTestPersistentActor(TestCqrsPersistentState(s))
          x <- getAll(a)
        } yield x shouldBe s
      }
      "not create snapshot" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval + SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield a.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(s, id)
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
        } yield {
          x should have size s.size
          y shouldBe No
          z should have size s.size
        }
      }
    }
    "CreateCmd" must {
      "change state" in {
        val state = Seq(generateString(), generateString())
        val s1 = generateString()
        for {
          a <- newTestPersistentActor(TestCqrsPersistentState(state))
          x <- ask(a, CreateCmd(s1))
          y <- getAll(a)
        } yield {
          x shouldBe Done
          y should contain(s1)
        }
      }
      "publish event" in {
        val state = Seq(generateString(), generateString())
        val s1 = generateString()
        val p = newSubscribedOnEventsTestProbe
        for {
          a <- newTestPersistentActor(TestCqrsPersistentState(state))
          x <- ask(a, CreateCmd(s1))
          y <- getAll(a)
        } yield {
          x shouldBe Done
          y should contain(s1)
          p.expectMsgType[CreatedEvt]
          succeed
        }
      }
    }
    "CreateCmd1" must {
      "change state" in {
        val state = Seq(generateString(), generateString())
        val s1 = generateString()
        for {
          a <- newTestPersistentActor(TestCqrsPersistentState(state))
          x <- ask(a, CreateCmd1(s1))
          y <- getAll(a)
        } yield {
          x shouldBe Done
          y should contain(s1)
        }
      }
      "publish event" in {
        val state = Seq(generateString(), generateString())
        val s1 = generateString()
        val p = newSubscribedOnEventsTestProbe
        for {
          a <- newTestPersistentActor(TestCqrsPersistentState(state))
          x <- ask(a, CreateCmd1(s1))
          y <- getAll(a)
        } yield {
          x shouldBe Done
          y should contain(s1)
          p.expectMsgType[CreatedEvt]
          succeed
        }
      }
    }
    "CreateCmd2" must {
      "change state" in {
        val state = Seq(generateString(), generateString())
        val s1 = generateString()
        for {
          a <- newTestPersistentActor(TestCqrsPersistentState(state))
          x <- ask(a, CreateCmd2(s1))
          y <- getAll(a)
        } yield {
          x shouldBe Done
          y should contain(s1)
        }
      }
      "publish event" in {
        val state = Seq(generateString(), generateString())
        val s1 = generateString()
        val p = newSubscribedOnEventsTestProbe
        for {
          a <- newTestPersistentActor(TestCqrsPersistentState(state))
          x <- ask(a, CreateCmd2(s1))
          y <- getAll(a)
        } yield {
          x shouldBe Done
          y should contain(s1)
          p.expectMsgType[CreatedEvt]
          succeed
        }
      }
    }
    "CreateCmd3" must {
      "change state" in {
        val state = Seq(generateString(), generateString())
        val s1 = generateString()
        for {
          a <- newTestPersistentActor(TestCqrsPersistentState(state))
          x <- ask(a, CreateCmd3(s1))
          y <- getAll(a)
        } yield {
          x shouldBe Done
          y should contain(s1)
        }
      }
      "publish event" in {
        val state = Seq(generateString(), generateString())
        val s1 = generateString()
        val p = newSubscribedOnEventsTestProbe
        for {
          a <- newTestPersistentActor(TestCqrsPersistentState(state))
          x <- ask(a, CreateCmd3(s1))
          y <- getAll(a)
        } yield {
          x shouldBe Done
          y should contain(s1)
          p.expectMsgType[CreatedEvt]
          succeed
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
        val id = ActorId(generateId)
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
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
        } yield {
          x should have size n
          y shouldBe Yes
          z should have size n
        }
      }
      "restore initState" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval + SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
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
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
        } yield {
          x should have size n + s.size
          y shouldBe Yes
          z should have size n + s.size
        }
      }
    }
    "recover" must {
      "recover initState" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
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
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
        }
      }
      "recover when created with initState empty" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
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
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
          i <- ask(c, GetRecoveredState).mapTo[Option[TestCqrsPersistentState]]
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
          i should not be empty
          i.get should have size n + s.size
        }
      }
      "afterRecover" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
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
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
          i <- ask(c, GetRecoveredState).mapTo[Option[TestCqrsPersistentState]]
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
          i should not be empty
          i.get should have size n + s.size
        }
      }
    }
    "recover (CreateCmd2)" must {
      "recover initState" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd2(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(s, id)
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
        }
      }
      "recover when created with initState empty" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd2(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(id = id) // empty initState
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
          i <- ask(c, GetRecoveredState).mapTo[Option[TestCqrsPersistentState]]
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
          i should not be empty
          i.get should have size n + s.size
        }
      }
      "afterRecover" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd2(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(s, id)
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
          i <- ask(c, GetRecoveredState).mapTo[Option[TestCqrsPersistentState]]
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
          i should not be empty
          i.get should have size n + s.size
        }
      }
    }

    "recover (CreateCmd3)" must {
      "recover initState" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd3(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(s, id)
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
        }
      }
      "recover when created with initState empty" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd3(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(id = id) // empty initState
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
          i <- ask(c, GetRecoveredState).mapTo[Option[TestCqrsPersistentState]]
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
          i should not be empty
          i.get should have size n + s.size
        }
      }
      "afterRecover" in {
        val id = ActorId(generateId)
        val n = SnapshotInterval / 2
        val s = TestCqrsPersistentState(Seq(generateString(), generateString()))
        for {
          a <- newTestPersistentActor(s, id)
          _ <- Future.sequence {
            for (i <- 1 to n) yield ask(a, CreateCmd3(generateString()))
          }
          x <- getAll(a)
          y <- ask(a, HasSnapshot)
          _ <- Future { a ! PoisonPill }
          _ <- Future { Thread.sleep(500) }
          c <- newTestPersistentActor(s, id)
          z <- c.ask(GetState).mapTo[TestCqrsPersistentState].map(_.getAll)
          i <- ask(c, GetRecoveredState).mapTo[Option[TestCqrsPersistentState]]
        } yield {
          x should have size n + s.size
          y shouldBe No
          z should have size n + s.size
          i should not be empty
          i.get should have size n + s.size
        }
      }
    }

    // todo: add probe test
    //    "Unknown command" must result {
    //      "UnknownCommand" in {
    //        for {
    //          a <- newTestPersistentActor()
    //          x <- ask(a, "unknown")
    //        } yield x shouldBe UnknownCommand
    //      }
    //    }
  }
}