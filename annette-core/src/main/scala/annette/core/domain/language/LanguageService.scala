package annette.core.domain.language

import akka.actor.Props
import annette.core.domain.language.model._
import annette.core.persistence.Persistence.{ PersistentCommand, PersistentEvent, PersistentQuery }

object LanguageService {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

  object EntryAlreadyExists extends Response
  object EntryNotFound extends Response

  case class CreateLanguageCmd(entry: Language) extends Command
  case class UpdateLanguageCmd(entry: LanguageUpdate) extends Command
  case class DeleteLanguageCmd(id: Language.Id) extends Command
  case class FindLanguageById(id: Language.Id) extends Query
  object FindAllLanguages extends Query

  case class LanguageCreatedEvt(entry: Language) extends Event
  case class LanguageUpdatedEvt(entry: LanguageUpdate) extends Event
  case class LanguageDeletedEvt(id: Language.Id) extends Event

  case class SingleLanguage(maybeEntry: Option[Language]) extends Response
  case class MultipleLanguages(entries: Map[Language.Id, Language]) extends Response

  def props(id: String, state: LanguageState = LanguageState()) = Props(classOf[LanguageActor], id, state)
}