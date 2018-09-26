package annette.core.domain.application

import annette.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse }

case class Application(name: String, code: String, id: Application.Id)

case class UpdateApplication(name: Option[String], code: Option[String], id: Application.Id)

object Application {
  type Id = String

  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case class CreateApplicationCmd(x: Application) extends Command
  case class UpdateApplicationCmd(x: UpdateApplication) extends Command
  case class DeleteApplicationCmd(x: Application.Id) extends Command

  case class GetApplicationById(x: Application.Id) extends Query
  object ListApplications extends Query

  case class ApplicationCreatedEvt(x: Application) extends Event
  case class ApplicationUpdatedEvt(x: UpdateApplication) extends Event
  case class ApplicationDeletedEvt(x: Application.Id) extends Event

  case class ApplicationCreated(x: Application) extends Response
  case class ApplicationOpt(x: Option[Application]) extends Response
  case class ApplicationsMap(x: Map[Application.Id, Application]) extends Response
  object EntryAlreadyExists extends Response
  object EntryNotFound extends Response
}

