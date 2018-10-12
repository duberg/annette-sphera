package annette.core.domain.tenancy.actor

import annette.core.akkaext.actor.CqrsState
import annette.core.domain.tenancy.LastSessionManager._
import annette.core.domain.tenancy.model.{ LastSession, User }
case class LastSessionManagerState(lastSessions: Map[User.Id, LastSession] = Map.empty) extends CqrsState {

  // both for updating and creating
  def storeLastSession(entry: LastSession): LastSessionManagerState = {
    copy(lastSessions = lastSessions + (entry.userId -> entry))
  }

  def findLastSessionByUserId(id: User.Id): Option[LastSession] = lastSessions.get(id)

  def findAllLastSessions: Seq[LastSession] = lastSessions.values.toSeq

  def lastSessionExists(id: User.Id): Boolean = lastSessions.get(id).isDefined

  def update: Update = {
    case LastSessionStoredEvt(entry) => storeLastSession(entry)
  }
}
