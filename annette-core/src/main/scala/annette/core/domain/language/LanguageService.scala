package annette.core.domain.language

import akka.Done
import akka.actor.{ ActorRef, Props }
import akka.util.Timeout
import annette.core.akkaext.actor._
import annette.core.domain.language.model.Language.Id
import annette.core.domain.language.model._
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.{ ExecutionContext, Future }
import LanguageService._
import akka.pattern.ask
import akka.util.Timeout

@Singleton
class LanguageService @Inject() (@Named("CoreService") actor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout) {
  def create(language: Language): Future[Unit] = {
    for {
      f <- ask(actor, CreateLanguageCmd(language))
    } yield {
      f match {
        case Done =>
        case EntryAlreadyExists => throw new LanguageAlreadyExists()
      }
    }
  }

  def update(language: LanguageUpdate): Future[Unit] = {
    for {
      f <- ask(actor, UpdateLanguageCmd(language))
    } yield {
      f match {
        case Done =>
        case EntryNotFound => throw new LanguageNotFound()
      }
    }
  }

  def getLanguageById(id: Language.Id): Future[Option[Language]] = {
    ask(actor, FindLanguageById(id)).mapTo[SingleLanguage].map(_.maybeEntry)
  }

  def selectAll: Future[List[Language]] = {
    ask(actor, FindAllLanguages).mapTo[MultipleLanguages].map(_.entries.values.toList)
  }

  def delete(id: Id): Future[Unit] = {
    for {
      f <- ask(actor, DeleteLanguageCmd(id))
    } yield {
      f match {
        case Done =>
        case EntryNotFound => throw new LanguageNotFound()
      }
    }
  }
}

object LanguageService {
  def props = Props(new LanguageActor())

  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

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
}
