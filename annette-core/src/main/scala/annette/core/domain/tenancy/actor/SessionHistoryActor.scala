package annette.core.domain.tenancy.actor

import akka.Done
import annette.core.akkaext.actor.ActorId
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.SessionHistoryService
import annette.core.domain.tenancy.model._
import SessionHistoryService._

class SessionHistoryActor(val id: ActorId, val initState: SessionHistoryState) extends CqrsPersistentActor[SessionHistoryState] {

  def createSessionHistory(state: SessionHistoryState, entry: SessionHistory): Unit = {
    if (state.sessionHistoryExists(entry.id)) sender ! EntryAlreadyExists
    else {
      persist(SessionHistoryCreatedEvt(entry)) { event =>
        changeState(state.updated(event))

        sender ! Done
      }
    }
  }

  def findSessionHistoryById(state: SessionHistoryState, id: OpenSession.Id): Unit =
    sender ! SessionHistoryOpt(state.findSessionHistoryById(id))

  def findAllSessionHistory(state: SessionHistoryState): Unit =
    sender ! SessionHistorySeq(state.findAllSessionHistory)

  def behavior(state: SessionHistoryState): Receive = {
    case CreateSessionHistoryCmd(entry) => createSessionHistory(state, entry)
    case FindSessionHistoryById(i) => findSessionHistoryById(state, i)
    case FindAllSessionHistory => findAllSessionHistory(state)

  }

}
