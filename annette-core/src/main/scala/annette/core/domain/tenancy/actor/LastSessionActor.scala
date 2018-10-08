package annette.core.domain.tenancy.actor

import akka.Done
import annette.core.akkaext.actor.ActorId
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.LastSessionService
import annette.core.domain.tenancy.model._
import LastSessionService._

class LastSessionActor(val id: ActorId, val initState: LastSessionState) extends CqrsPersistentActor[LastSessionState] {

  def storeLastSession(state: LastSessionState, entry: LastSession): Unit = {
    persist(LastSessionStoredEvt(entry)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  def findLastSessionByUserId(state: LastSessionState, id: User.Id): Unit =
    sender ! LastSessionOpt(state.findLastSessionByUserId(id))

  def findAllLastSessions(state: LastSessionState): Unit =
    sender ! LastSessionSeq(state.findAllLastSessions)

  def behavior(state: LastSessionState): Receive = {
    case StoreLastSessionCmd(entry) => storeLastSession(state, entry)
    case FindLastSessionByUserId(i) => findLastSessionByUserId(state, i)
    case FindAllLastSessions => findAllLastSessions(state)

  }

}
