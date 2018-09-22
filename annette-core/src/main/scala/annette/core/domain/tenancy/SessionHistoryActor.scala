package annette.core.domain.tenancy

import akka.Done
import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence._

class SessionHistoryActor(val id: String, val initState: SessionHistoryState) extends PersistentStateActor[SessionHistoryState] {

  def createSessionHistory(state: SessionHistoryState, entry: SessionHistory): Unit = {
    if (state.sessionHistoryExists(entry.id)) sender ! SessionHistoryService.EntryAlreadyExists
    else {
      persist(SessionHistoryService.SessionHistoryCreatedEvt(entry)) { event =>
        changeState(state.updated(event))

        sender ! Done
      }
    }
  }

  def findSessionHistoryById(state: SessionHistoryState, id: OpenSession.Id): Unit =
    sender ! SessionHistoryService.SessionHistoryOpt(state.findSessionHistoryById(id))

  def findAllSessionHistory(state: SessionHistoryState): Unit =
    sender ! SessionHistoryService.SessionHistorySeq(state.findAllSessionHistory)

  def behavior(state: SessionHistoryState): Receive = {
    case SessionHistoryService.CreateSessionHistoryCmd(entry) => createSessionHistory(state, entry)
    case SessionHistoryService.FindSessionHistoryById(i) => findSessionHistoryById(state, i)
    case SessionHistoryService.FindAllSessionHistory => findAllSessionHistory(state)

  }

}
