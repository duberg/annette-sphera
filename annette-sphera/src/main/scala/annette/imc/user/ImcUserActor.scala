package annette.imc.user

import java.util.UUID

import akka.actor.Props
import annette.core.persistence.Persistence._
import annette.imc.user.model.ImcUser

class ImcUserActor(val id: String, val initState: ImcUserState) extends PersistentStateActor[ImcUserState] {
  override def persistenceId: String = s"imc-user-$id-1"
  import ImcUserActor._
  def create(state: ImcUserState, entry: ImcUser): Unit = {
    if (state.exists(entry.id)) sender ! EntryAlreadyExists
    else {
      persist(CreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }
  def update(state: ImcUserState, entry: ImcUser): Unit = {

    if (state.exists(entry.id)) {
      persist(UpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! EntryNotFound
  }

  def delete(state: ImcUserState, id: UUID): Unit = {
    if (state.exists(id)) {
      persist(DeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! EntryNotFound
  }

  def findById(state: ImcUserState, id: UUID): Unit =
    sender ! SingleEntry(state.getById(id))

  def findAll(state: ImcUserState): Unit =
    sender ! MultipleEntries(state.getAll)

  //  def clear(state: CharacteristicState): Unit = {
  //    persist(CharacteristicRepositoryActor.ClearEvt()) { event =>
  //      changeState(state.updated(event))
  //      sender ! CharacteristicRepositoryActor.Done
  //    }
  //  }

  def behavior(state: ImcUserState): Receive = {
    case CreateCmd(x) => create(state, x)
    case UpdateCmd(x) => update(state, x)
    case DeleteCmd(x) => delete(state, x)
    case GetById(x) => findById(state, x)
    case GetAll => findAll(state)
    //    case CharacteristicRepositoryActor.ClearCmd => clear(state)
  }
}

object ImcUserActor {
  trait Command extends PersistentCommand
  trait Request extends PersistentQuery
  trait Response extends PersistentResponse
  trait Event extends PersistentEvent

  case class CreateCmd(entry: ImcUser) extends Command
  case class UpdateCmd(entry: ImcUser) extends Command
  case class DeleteCmd(id: UUID) extends Command

  case class GetById(id: UUID) extends Request
  object GetAll extends Request

  case class CreatedEvt(entry: ImcUser) extends Event
  case class UpdatedEvt(entry: ImcUser) extends Event
  case class DeletedEvt(id: UUID) extends Event

  object Done extends Response
  case class SingleEntry(maybeEntry: Option[ImcUser]) extends Response
  case class MultipleEntries(entries: Map[UUID, ImcUser]) extends Response
  object EntryAlreadyExists extends Response
  object EntryNotFound extends Response

  def props(id: String, state: ImcUserState = ImcUserState()) = Props(new ImcUserActor(id, state))
}

