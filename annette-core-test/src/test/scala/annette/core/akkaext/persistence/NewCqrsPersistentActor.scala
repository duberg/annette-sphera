package annette.core.akkaext.persistence

import akka.actor.{ ActorRef, Props }
import akka.persistence.SnapshotMetadata
import akka.testkit.{ TestKit, TestProbe }
import akka.util.Timeout
import annette.core.akkaext.actor.CqrsQuery.GetState
import annette.core.akkaext.actor._
import annette.core.akkaext.persistence.TestCqrsPersistentActor._
import annette.core.test.PersistenceSpec

import scala.concurrent.{ ExecutionContext, Future }

case class TestCqrsPersistentState(storage: Seq[String] = Seq.empty) extends CqrsState {
  def create(x: String): TestCqrsPersistentState = {
    if (storage contains x) throw new IllegalArgumentException
    copy(storage :+ x)
  }

  def exists(x: String): Boolean = storage contains x

  def getAll: Seq[String] = storage

  def update = PartialFunction.empty

  override def postUpdate = {
    case CreatedEvt(x) => create(x)
  }

  def size: Int = storage.size
}

class TestCqrsPersistentActor(override val initState: TestCqrsPersistentState)(implicit val c: ExecutionContext, val t: Timeout) extends CqrsPersistentActor[TestCqrsPersistentState] {
  var snapshotCreated: Boolean = false
  var recoveredState: Option[TestCqrsPersistentState] = None

  def create(state: TestCqrsPersistentState, x: String): Unit = {
    if (state.exists(x)) sender ! EntryAlreadyExists
    else {
      val event = CreatedEvt(x)

      persist(state, event) { (state, event) =>
        publish(event)
        sender() ! Done
      }
    }

    //    if (state.exists(x)) sender ! EntryAlreadyExists
    //    else {
    //      val event = CreatedEvt(x)
    //      val f = for {
    //        r1 <- self.publish(event)
    //        r2 <- self.persist(event)
    //      } yield Done
    //
    //      f pipeTo sender()
    //    }
  }

  def create1(state: TestCqrsPersistentState, x: String): Unit = {
    if (state.exists(x)) sender ! EntryAlreadyExists
    else {
      val event = CreatedEvt(x)
      persistAfter(Future.successful(1), event) { (response, state, event) =>
        publish(event)
        sender() ! Done
      }
    }
  }

  def create2(state: TestCqrsPersistentState, x: String): Unit = {
    if (state.exists(x)) sender ! EntryAlreadyExists
    else {
      val event = CreatedEvt(x)
      persistAfter(Future.successful(1), event) { (response, state, event) =>
        publish(event)
        context.actorOf(TestCqrsPersistentActor.props())
        sender() ! Done
      }
    }
  }

  def create3(state: TestCqrsPersistentState, x: String): Unit = {
    if (state.exists(x)) sender ! EntryAlreadyExists
    else {
      val event = CreatedEvt(x)
      persist(state, event) { (state, event) =>
        publish(event)
        context.actorOf(TestCqrsPersistentActor.props())
        sender() ! Done
      }
    }
  }

  def behavior(state: TestCqrsPersistentState): Receive = {
    case CreateCmd(x) => create(state, x)
    case CreateCmd1(x) => create1(state, x)
    case CreateCmd2(x) => create2(state, x)
    case CreateCmd3(x) => create3(state, x)
    case HasSnapshot => if (snapshotCreated) sender() ! Yes else sender() ! No
    case GetRecoveredState =>
      sender() ! recoveredState
  }

  override def afterSnapshot(metadata: SnapshotMetadata, success: Boolean): Unit = if (success) snapshotCreated = true
  override def afterRecover(state: TestCqrsPersistentState): Unit = recoveredState = Option(state)
}

object TestCqrsPersistentActor {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateCmd(x: String) extends Command
  case class CreateCmd1(x: String) extends Command
  case class CreateCmd2(x: String) extends Command
  case class CreateCmd3(x: String) extends Command

  case object HasSnapshot extends Query
  case object GetRecoveredState extends Query

  case class CreatedEvt(x: String) extends Event

  case object Done extends Response
  case object EntryAlreadyExists extends Response
  case object Yes extends Response
  case object No extends Response

  def props(state: TestCqrsPersistentState = TestCqrsPersistentState())(implicit executor: ExecutionContext, timeout: Timeout) =
    Props(new TestCqrsPersistentActor(state))
}

trait NewCqrsPersistentActor { _: PersistenceSpec with TestKit =>
  def getAll(a: ActorRef): Future[Seq[String]] = {
    ask(a, GetState)
      .mapTo[TestCqrsPersistentState]
      .map(_.getAll)
  }

  def newTestPersistentActor(id: String = generateString(), state: TestCqrsPersistentState = TestCqrsPersistentState()): Future[ActorRef] = Future {
    system.actorOf(TestCqrsPersistentActor.props(state), id)
  }

  def generateString(): String = s"str-$generateId"

  def subscribeOnEvents(p: TestProbe): TestProbe = {
    system.eventStream.subscribe(p.ref, classOf[Event])
    p
  }

  def newSubscribedOnEventsTestProbe: TestProbe = {
    val p = newTestProbe
    subscribeOnEvents(p)
  }
}