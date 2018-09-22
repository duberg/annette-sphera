package annette.core.persistence

import java.util.UUID

import akka.actor.{ ActorRef, Props }
import akka.pattern.ask
import akka.persistence.SnapshotMetadata
import akka.testkit.TestKit
import akka.util.Timeout
import annette.core.test.PersistenceSpec
import annette.core.persistence.Persistence._

import scala.concurrent.{ ExecutionContext, Future }

case class TestPersistentStateActor(storage: Seq[String] = Seq.empty) extends PersistentState[TestPersistentStateActor] {
  import TestPersistentActor._

  def create(x: String): TestPersistentStateActor = {
    if (storage contains x) throw new IllegalArgumentException
    copy(storage :+ x)
  }

  def exists(x: String): Boolean = storage contains x

  def getAll: Seq[String] = storage

  def updated(event: PersistentEvent): TestPersistentStateActor = event match {
    case CreatedEvt(x) => create(x)
  }

  def size: Int = storage.size
}

class TestPersistentActor(
  val id: String,
  val initState: TestPersistentStateActor)(implicit val executor: ExecutionContext, val timeout: Timeout) extends PersistentStateActor[TestPersistentStateActor] {
  var snapshotCreated: Boolean = false
  var recoveredState: Option[TestPersistentStateActor] = None
  import TestPersistentActor._

  def create(state: TestPersistentStateActor, x: String): Unit = {
    if (state.exists(x)) sender ! EntryAlreadyExists
    else {
      persist(CreatedEvt(x)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }

  def findAll(state: TestPersistentStateActor): Unit =
    sender ! MultipleEntries(state.getAll)

  def behavior(state: TestPersistentStateActor): Receive = {
    case CreateCmd(x) => create(state, x)
    case GetAll => findAll(state)
    case HasSnapshot =>
      if (snapshotCreated) sender() ! Yes else sender() ! No
    case GetRecoveredState =>
      sender() ! recoveredState
  }

  override def afterSnapshot(metadata: SnapshotMetadata, success: Boolean): Unit = if (success) snapshotCreated = true

  override def afterRecover(state: TestPersistentStateActor): Unit = recoveredState = Option(state)
}

object TestPersistentActor {
  trait Command extends PersistentCommand
  trait Query extends PersistentQuery
  trait Response extends PersistentResponse
  trait Event extends PersistentEvent

  case class CreateCmd(x: String) extends Command

  case object GetAll extends Query
  case object HasSnapshot extends Query
  case object GetRecoveredState extends Query

  case class CreatedEvt(x: String) extends PersistentEvent

  case object Done extends Response
  case object EntryAlreadyExists extends Response
  case object Yes extends Response
  case object No extends Response
  case class MultipleEntries(entries: Seq[String]) extends Response

  def props(id: String, state: TestPersistentStateActor = TestPersistentStateActor())(implicit executor: ExecutionContext, timeout: Timeout) =
    Props(new TestPersistentActor(id, state))
}

trait NewPersistentStateActor { _: PersistenceSpec with TestKit =>
  import TestPersistentActor._

  def getAll(a: ActorRef): Future[Seq[String]] = ask(a, GetAll).mapTo[MultipleEntries].map(_.entries)
  def getRelated(a: ActorRef): Future[Subscribers] = ask(a, GetSubscriber).mapTo[SubscribersMap].map(_.x)
  def addRelated(a: ActorRef, x: UUID, y: ActorRef): Future[Any] = ask(a, AddSubscriber(x, y))
  def removeRelated(a: ActorRef, x: ActorRef): Future[Any] = ask(a, RemoveSubscriberByActorRef(x))

  def newTestPersistentActor(
    state: TestPersistentStateActor = TestPersistentStateActor(),
    id: String = s"TestPersistentActor-$generateUUID"): Future[ActorRef] = Future {
    system.actorOf(TestPersistentActor.props(id, state), id)
  }

  def generateString(): String = s"str-$generateId"
}