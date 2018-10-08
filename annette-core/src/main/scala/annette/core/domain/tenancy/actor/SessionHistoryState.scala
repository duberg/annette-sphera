package annette.core.domain.tenancy.actor

import annette.core.akkaext.actor.CqrsState
import annette.core.domain.tenancy.SessionHistoryService
import annette.core.domain.tenancy.model.{ OpenSession, SessionHistory }

case class SessionHistoryState(sessionHistory: Map[OpenSession.Id, SessionHistory] = Map.empty) extends CqrsState {

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

  def update: Update = {
    case SessionHistoryService.SessionHistoryCreatedEvt(entry) => createSessionHistory(entry)
  }
}
