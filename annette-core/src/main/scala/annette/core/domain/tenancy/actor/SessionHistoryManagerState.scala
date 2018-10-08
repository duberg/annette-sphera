package annette.core.domain.tenancy.actor

import annette.core.akkaext.actor.CqrsState
import annette.core.domain.tenancy.SessionHistoryManager
import annette.core.domain.tenancy.model.{ OpenSession, SessionHistory }

case class SessionHistoryManagerState(sessionHistory: Map[OpenSession.Id, SessionHistory] = Map.empty) extends CqrsState {

  def createSessionHistory(entry: SessionHistory): SessionHistoryManagerState = {
    if (sessionHistory.get(entry.id).isDefined) throw new IllegalArgumentException
    else copy(sessionHistory = sessionHistory + (entry.id -> entry))
  }

  def deleteSessionHistory(id: OpenSession.Id): SessionHistoryManagerState = {
    if (sessionHistory.get(id).isEmpty) throw new IllegalArgumentException
    else copy(sessionHistory = sessionHistory - id)
  }

  def findSessionHistoryById(id: OpenSession.Id): Option[SessionHistory] = sessionHistory.get(id)

  def findAllSessionHistory: Seq[SessionHistory] = sessionHistory.values.toSeq

  def sessionHistoryExists(id: OpenSession.Id): Boolean = sessionHistory.get(id).isDefined

  def update: Update = {
    case SessionHistoryManager.SessionHistoryCreatedEvt(entry) => createSessionHistory(entry)
  }
}
