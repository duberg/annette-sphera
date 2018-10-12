package annette.core.domain.tenancy.actor

import akka.Done
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.LastSessionManager
import annette.core.domain.tenancy.LastSessionManager._
import annette.core.domain.tenancy.model._

class LastSessionManagerActor(val initState: LastSessionManagerState = LastSessionManagerState()) extends CqrsPersistentActor[LastSessionManagerState] {
  def storeLastSession(state: LastSessionManagerState, entry: LastSession): Unit = {
    persist(LastSessionStoredEvt(entry)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  def findLastSessionByUserId(state: LastSessionManagerState, id: User.Id): Unit =
    sender ! LastSessionOpt(state.findLastSessionByUserId(id))

  def findAllLastSessions(state: LastSessionManagerState): Unit =
    sender ! LastSessionSeq(state.findAllLastSessions)

  def behavior(state: LastSessionManagerState): Receive = {
    case StoreLastSessionCmd(entry) => storeLastSession(state, entry)
    case FindLastSessionByUserId(i) => findLastSessionByUserId(state, i)
    case FindAllLastSessions => findAllLastSessions(state)

  }

}
