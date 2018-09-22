package annette.core.domain.language

import akka.Done
import annette.core.domain.language.model._
import annette.core.persistence.Persistence._

class LanguageActor(val id: String, val initState: LanguageState) extends PersistentStateActor[LanguageState] {

  def createLanguage(state: LanguageState, entry: Language): Unit = {
    if (state.languageExists(entry.id)) sender ! LanguageService.EntryAlreadyExists
    else {
      persist(LanguageService.LanguageCreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }

  def updateLanguage(state: LanguageState, entry: LanguageUpdate): Unit = {
    if (state.languageExists(entry.id)) {
      persist(LanguageService.LanguageUpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! LanguageService.EntryNotFound
    }
  }

  def deleteLanguage(state: LanguageState, id: Language.Id): Unit = {
    if (state.languageExists(id)) {
      persist(LanguageService.LanguageDeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! LanguageService.EntryNotFound
    }
  }

  def findLanguageById(state: LanguageState, id: Language.Id): Unit =
    sender ! LanguageService.SingleLanguage(state.findLanguageById(id))

  def findAllLanguages(state: LanguageState): Unit =
    sender ! LanguageService.MultipleLanguages(state.findAllLanguages)

  def behavior(state: LanguageState): Receive = {
    case LanguageService.CreateLanguageCmd(entry) => createLanguage(state, entry)
    case LanguageService.UpdateLanguageCmd(entry) => updateLanguage(state, entry)
    case LanguageService.DeleteLanguageCmd(id) => deleteLanguage(state, id)
    case LanguageService.FindLanguageById(id) => findLanguageById(state, id)
    case LanguageService.FindAllLanguages => findAllLanguages(state)

  }

}
