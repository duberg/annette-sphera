package annette.core.domain.application

import akka.Done
import annette.core.akkaext.actor.ActorId
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.application.Application._

class ApplicationManagerActor(val id: ActorId, val initState: ApplicationManagerState) extends CqrsPersistentActor[ApplicationManagerState] {
  def createApplication(state: ApplicationManagerState, x: Application): Unit = {
    if (state.applicationExists(x.id)) sender ! EntryAlreadyExists
    else {
      persist(state, ApplicationCreatedEvt(x)) { (state, event) =>
        sender ! ApplicationCreated(x)
      }
    }
  }

  def updateApplication(state: ApplicationManagerState, entry: UpdateApplication): Unit = {
    if (state.applicationExists(entry.id)) {
      persist(ApplicationUpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! EntryNotFound
    }
  }

  def deleteApplication(state: ApplicationManagerState, id: Application.Id): Unit = {
    if (state.applicationExists(id)) {
      persist(ApplicationDeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! EntryNotFound
    }
  }

  def findApplicationById(state: ApplicationManagerState, id: Application.Id): Unit = {
    sender ! ApplicationOpt(state.findApplicationById(id))
  }

  def listApplications(state: ApplicationManagerState): Unit = {
    sender ! ApplicationsMap(state.findAllApplications)
  }

  def behavior(state: ApplicationManagerState): Receive = {
    case CreateApplicationCmd(entry) => createApplication(state, entry)
    case UpdateApplicationCmd(entry) => updateApplication(state, entry)
    case DeleteApplicationCmd(id) => deleteApplication(state, id)
    case GetApplicationById(id) => findApplicationById(state, id)
    case ListApplications => listApplications(state)
  }
}
