package annette.core.domain.tenancy

import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence
import annette.core.persistence.Persistence.PersistentState

case class SessionHistoryState(
  sessionHistory: Map[OpenSession.Id, SessionHistory] = Map.empty) extends PersistentState[SessionHistoryState] {

  def createSessionHistory(entry: SessionHistory): SessionHistoryState = {
    if (sessionHistory.get(entry.id).isDefined) throw new IllegalArgumentException
    else copy(sessionHistory = sessionHistory + (entry.id -> entry))
  }

  def deleteSessionHistory(id: OpenSession.Id): SessionHistoryState = {
    if (sessionHistory.get(id).isEmpty) throw new IllegalArgumentException
    else copy(sessionHistory = sessionHistory - id)
  }

  def findSessionHistoryById(id: OpenSession.Id): Option[SessionHistory] = sessionHistory.get(id)

  def findAllSessionHistory: Seq[SessionHistory] = sessionHistory.values.toSeq

  def sessionHistoryExists(id: OpenSession.Id): Boolean = sessionHistory.get(id).isDefined

  override def updated(event: Persistence.PersistentEvent): SessionHistoryState = {
    event match {
      case SessionHistoryService.SessionHistoryCreatedEvt(entry) => createSessionHistory(entry)
    }
  }
}