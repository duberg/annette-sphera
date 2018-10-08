
package annette.core.domain.language

import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.domain.language.model.Language.Id
import annette.core.domain.language.model.{ Language, LanguageUpdate }
import javax.inject._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class LanguageManager @Inject() (@Named("CoreService") actor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout) {

  def create(language: Language): Future[Unit] = {
    for {
      f <- ask(actor, LanguageService.CreateLanguageCmd(language))
    } yield {
      f match {
        case Done =>
        case LanguageService.EntryAlreadyExists => throw new LanguageAlreadyExists()
      }
    }
  }

  def update(language: LanguageUpdate): Future[Unit] = {
    for {
      f <- ask(actor, LanguageService.UpdateLanguageCmd(language))
    } yield {
      f match {
        case Done =>
        case LanguageService.EntryNotFound => throw new LanguageNotFound()
      }
    }
  }

  def getById(id: Language.Id): Future[Option[Language]] = {
    ask(actor, LanguageService.FindLanguageById(id)).mapTo[LanguageService.SingleLanguage].map(_.maybeEntry)
  }

  def selectAll: Future[List[Language]] = {
    ask(actor, LanguageService.FindAllLanguages).mapTo[LanguageService.MultipleLanguages].map(_.entries.values.toList)
  }

  def delete(id: Id): Future[Unit] = {
    for {
      f <- ask(actor, LanguageService.DeleteLanguageCmd(id))
    } yield {
      f match {
        case Done =>
        case LanguageService.EntryNotFound => throw new LanguageNotFound()
      }
    }
  }
}
