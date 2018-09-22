package annette.core.domain.application

import akka.Done
import annette.core.domain.application.model._
import annette.core.persistence.Persistence._

class ApplicationActor(val id: String, val initState: ApplicationState) extends PersistentStateActor[ApplicationState] {

  def createApplication(state: ApplicationState, entry: Application): Unit = {
    if (state.applicationExists(entry.id)) sender ! ApplicationService.EntryAlreadyExists
    else {
      persist(ApplicationService.ApplicationCreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }

  def updateApplication(state: ApplicationState, entry: ApplicationUpdate): Unit = {
    if (state.applicationExists(entry.id)) {
      persist(ApplicationService.ApplicationUpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! ApplicationService.EntryNotFound
    }
  }

  def deleteApplication(state: ApplicationState, id: Application.Id): Unit = {
    if (state.applicationExists(id)) {
      persist(ApplicationService.ApplicationDeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! ApplicationService.EntryNotFound
    }
  }

  def findApplicationById(state: ApplicationState, id: Application.Id): Unit =
    sender ! ApplicationService.SingleApplication(state.findApplicationById(id))

  def findAllApplications(state: ApplicationState): Unit =
    sender ! ApplicationService.MultipleApplications(state.findAllApplications)

  def behavior(state: ApplicationState): Receive = {
    case ApplicationService.CreateApplicationCmd(entry) => createApplication(state, entry)
    case ApplicationService.UpdateApplicationCmd(entry) => updateApplication(state, entry)
    case ApplicationService.DeleteApplicationCmd(id) => deleteApplication(state, id)
    case ApplicationService.FindApplicationById(id) => findApplicationById(state, id)
    case ApplicationService.FindAllApplications => findAllApplications(state)

  }

}
