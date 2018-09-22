package annette.core.domain.tenancy

import akka.Done
import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence._

class LastSessionActor(val id: String, val initState: LastSessionState) extends PersistentStateActor[LastSessionState] {

  def storeLastSession(state: LastSessionState, entry: LastSession): Unit = {
    persist(LastSessionService.LastSessionStoredEvt(entry)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  def findLastSessionByUserId(state: LastSessionState, id: User.Id): Unit =
    sender ! LastSessionService.LastSessionOpt(state.findLastSessionByUserId(id))

  def findAllLastSessions(state: LastSessionState): Unit =
    sender ! LastSessionService.LastSessionSeq(state.findAllLastSessions)

  def behavior(state: LastSessionState): Receive = {
    case LastSessionService.StoreLastSessionCmd(entry) => storeLastSession(state, entry)
    case LastSessionService.FindLastSessionByUserId(i) => findLastSessionByUserId(state, i)
    case LastSessionService.FindAllLastSessions => findAllLastSessions(state)

  }

}
