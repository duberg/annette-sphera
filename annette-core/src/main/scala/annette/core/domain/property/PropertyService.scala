package annette.core.domain.property
import java.util.UUID

import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.{ Tenant, User }
import akka.actor.Props
import annette.core.akkaext.actor.{ ActorId, CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse }
import annette.core.domain.application.Application
import annette.core.domain.property.model._
import annette.core.utils.{ AnyValue, FilterOption, NoValue }

object PropertyService {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  object EntryAlreadyExists extends Response
  object EntryNotFound extends Response

  case class SetPropertyCmd(entry: Property) extends Command
  case class RemovePropertyCmd(id: Property.Id) extends Command
  case class FindPropertyById(id: Property.Id) extends Query
  case class FindProperties(
    userId: FilterOption[User.Id] = NoValue,
    tenantId: FilterOption[Tenant.Id] = NoValue,
    applicationId: FilterOption[Application.Id] = NoValue,
    languageId: FilterOption[Language.Id] = NoValue,
    key: FilterOption[String] = AnyValue) extends Query
  object FindAllProperties extends Query

  case class PropertySetEvt(entry: Property) extends Event
  case class PropertyRemovedEvt(id: Property.Id) extends Event

  case class PropertyOption(maybeEntry: Option[Property]) extends Response
  case class PropertySeq(entries: Seq[Property]) extends Response

  def props(id: ActorId, state: PropertyState = PropertyState()) =
    Props(new PropertyActor(id, state))
}

