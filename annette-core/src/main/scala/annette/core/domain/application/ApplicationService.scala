package annette.core.domain.application

import akka.actor.Props
import annette.core.domain.application.model._
import annette.core.persistence.Persistence.{ PersistentCommand, PersistentEvent, PersistentQuery }

object ApplicationService {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

  object EntryAlreadyExists extends Response
  object EntryNotFound extends Response

  case class CreateApplicationCmd(entry: Application) extends Command
  case class UpdateApplicationCmd(entry: ApplicationUpdate) extends Command
  case class DeleteApplicationCmd(id: Application.Id) extends Command
  case class FindApplicationById(id: Application.Id) extends Query
  object FindAllApplications extends Query

  case class ApplicationCreatedEvt(entry: Application) extends Event
  case class ApplicationUpdatedEvt(entry: ApplicationUpdate) extends Event
  case class ApplicationDeletedEvt(id: Application.Id) extends Event

  case class SingleApplication(maybeEntry: Option[Application]) extends Response
  case class MultipleApplications(entries: Map[Application.Id, Application]) extends Response

  def props(id: String, state: ApplicationState = ApplicationState()) = Props(classOf[ApplicationActor], id, state)
}
