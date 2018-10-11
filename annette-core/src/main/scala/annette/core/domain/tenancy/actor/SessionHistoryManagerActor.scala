package annette.core.domain.tenancy.actor

import akka.Done
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.SessionHistoryManager
import annette.core.domain.tenancy.model._
import SessionHistoryManager._

class SessionHistoryManagerActor(val initState: SessionHistoryManagerState = SessionHistoryManagerState()) extends CqrsPersistentActor[SessionHistoryManagerState] {
  def createSessionHistory(state: SessionHistoryManagerState, entry: SessionHistory): Unit = {
    if (state.sessionHistoryExists(entry.id)) sender ! EntryAlreadyExists
    else {
      persist(SessionHistoryCreatedEvt(entry)) { event =>
        changeState(state.updated(event))

        sender ! Done
      }
    }
  }

  def findSessionHistoryById(state: SessionHistoryManagerState, id: OpenSession.Id): Unit =
    sender ! SessionHistoryOpt(state.findSessionHistoryById(id))

  def findAllSessionHistory(state: SessionHistoryManagerState): Unit =
    sender ! SessionHistorySeq(state.findAllSessionHistory)

  def behavior(state: SessionHistoryManagerState): Receive = {
    case CreateSessionHistoryCmd(entry) => createSessionHistory(state, entry)
    case FindSessionHistoryById(i) => findSessionHistoryById(state, i)
    case FindAllSessionHistory => findAllSessionHistory(state)
  }
}
