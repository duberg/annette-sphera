package annette.core.domain.application

import annette.core.akkaext.actor.CqrsState
import annette.core.domain.application.Application._

case class ApplicationManagerState(applications: Map[Application.Id, Application] = Map.empty) extends CqrsState {
  def createApplication(entry: Application): ApplicationManagerState = {
    if (applications.get(entry.id).isDefined) throw new IllegalArgumentException
    else copy(applications = applications + (entry.id -> entry))
  }

  def updateApplication(entry: UpdateApplication): ApplicationManagerState = {
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

  def deleteApplication(id: Application.Id): ApplicationManagerState = {
    if (applications.get(id).isEmpty) throw new IllegalArgumentException
    else copy(applications = applications - id)
  }

  def findApplicationById(id: Application.Id): Option[Application] = applications.get(id)

  def findAllApplications: Map[Application.Id, Application] = applications

  def applicationExists(id: Application.Id): Boolean = applications.get(id).isDefined

  def update: Update = {
    case ApplicationCreatedEvt(entry) => createApplication(entry)
    case ApplicationUpdatedEvt(entry) => updateApplication(entry)
    case ApplicationDeletedEvt(id) => deleteApplication(id)
  }
}