package annette.core.domain.tenancy

import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence
import annette.core.persistence.Persistence.PersistentState

case class LastSessionState(
  lastSessions: Map[User.Id, LastSession] = Map.empty) extends PersistentState[LastSessionState] {

  // both for updating and creating
  def storeLastSession(entry: LastSession): LastSessionState = {
    copy(lastSessions = lastSessions + (entry.userId -> entry))
  }

  def findLastSessionByUserId(id: User.Id): Option[LastSession] = lastSessions.get(id)

  def findAllLastSessions: Seq[LastSession] = lastSessions.values.toSeq

  def lastSessionExists(id: User.Id): Boolean = lastSessions.get(id).isDefined

  override def updated(event: Persistence.PersistentEvent): LastSessionState = {
    event match {
      case LastSessionService.LastSessionStoredEvt(entry) => storeLastSession(entry)
    }
  }
}