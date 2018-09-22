package annette.core.domain.language

import annette.core.domain.language.model._
import annette.core.persistence.Persistence
import annette.core.persistence.Persistence.PersistentState

case class LanguageState(
  languages: Map[Language.Id, Language] = Map.empty) extends PersistentState[LanguageState] {

  def createLanguage(entry: Language): LanguageState = {
    if (languages.get(entry.id).isDefined) throw new IllegalArgumentException
    else copy(languages = languages + (entry.id -> entry))
  }

  def updateLanguage(entry: LanguageUpdate): LanguageState = {
    languages
      .get(entry.id)
      .map {
        e =>
          val updatedEntry = e.copy(
            name = entry.name.getOrElse(e.name))
          copy(languages = languages + (entry.id -> updatedEntry))
      }
      .getOrElse(throw new IllegalArgumentException)
  }

  def deleteLanguage(id: Language.Id): LanguageState = {
    if (languages.get(id).isEmpty) throw new IllegalArgumentException
    else copy(languages = languages - id)
  }

  def findLanguageById(id: Language.Id): Option[Language] = languages.get(id)

  def findAllLanguages: Map[Language.Id, Language] = languages

  def languageExists(id: Language.Id): Boolean = languages.get(id).isDefined

  override def updated(event: Persistence.PersistentEvent) = {
    event match {
      case LanguageService.LanguageCreatedEvt(entry) => createLanguage(entry)
      case LanguageService.LanguageUpdatedEvt(entry) => updateLanguage(entry)
      case LanguageService.LanguageDeletedEvt(id) => deleteLanguage(id)
    }
  }
}