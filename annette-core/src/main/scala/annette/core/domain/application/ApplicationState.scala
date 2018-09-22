package annette.core.domain.application

import annette.core.domain.application.model._
import annette.core.persistence.Persistence
import annette.core.persistence.Persistence.PersistentState

case class ApplicationState(
  applications: Map[Application.Id, Application] = Map.empty) extends PersistentState[ApplicationState] {

  def createApplication(entry: Application): ApplicationState = {
    if (applications.get(entry.id).isDefined) throw new IllegalArgumentException
    else copy(applications = applications + (entry.id -> entry))
  }

  def updateApplication(entry: ApplicationUpdate): ApplicationState = {
    applications
      .get(entry.id)
      .map {
        e =>
          val updatedEntry = e.copy(
            name = entry.name.getOrElse(e.name),
            code = entry.code.getOrElse(e.code))
          copy(applications = applications + (entry.id -> updatedEntry))
      }
      .getOrElse(throw new IllegalArgumentException)
  }

  def deleteApplication(id: Application.Id): ApplicationState = {
    if (applications.get(id).isEmpty) throw new IllegalArgumentException
    else copy(applications = applications - id)
  }

  def findApplicationById(id: Application.Id): Option[Application] = applications.get(id)

  def findAllApplications: Map[Application.Id, Application] = applications

  def applicationExists(id: Application.Id): Boolean = applications.get(id).isDefined

  override def updated(event: Persistence.PersistentEvent) = {
    event match {
      case ApplicationService.ApplicationCreatedEvt(entry) => createApplication(entry)
      case ApplicationService.ApplicationUpdatedEvt(entry) => updateApplication(entry)
      case ApplicationService.ApplicationDeletedEvt(id) => deleteApplication(id)
    }
  }
}