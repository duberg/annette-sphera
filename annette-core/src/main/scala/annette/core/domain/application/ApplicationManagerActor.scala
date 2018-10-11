package annette.core.domain.application

import akka.Done
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.application.Application._

class ApplicationManagerActor(val initState: ApplicationManagerState = ApplicationManagerState()) extends CqrsPersistentActor[ApplicationManagerState] {
  def createApplication(p1: ApplicationManagerState, p2: Application): Unit = {
    if (p1.applicationExists(p2.id)) sender ! EntryAlreadyExists
    else persist(p1, ApplicationCreatedEvt(p2)) { (state, event) =>
      sender ! ApplicationCreated(p2)
    }
  }

  def updateApplication(p1: ApplicationManagerState, p2: UpdateApplication): Unit = {
    if (p1.applicationExists(p2.id)) persist(p1, ApplicationUpdatedEvt(p2)) { (state, event) =>
      sender ! Done
    }
    else sender ! EntryNotFound
  }

  def deleteApplication(p1: ApplicationManagerState, p2: Application.Id): Unit = {
    if (p1.applicationExists(p2)) persist(p1, ApplicationDeletedEvt(p2)) { (state, event) =>
      sender ! Done
    }
    else sender ! EntryNotFound
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
