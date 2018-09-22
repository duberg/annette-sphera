package annette.core.domain.property

import akka.Done
import annette.core.domain.application.model.Application
import annette.core.domain.language.model.Language
import annette.core.domain.property.model._
import annette.core.domain.tenancy.model.{ Tenant, User }
import annette.core.persistence.Persistence._
import annette.core.utils.FilterOption

class PropertyActor(val id: String, val initState: PropertyState) extends PersistentStateActor[PropertyState] {

  def setProperty(state: PropertyState, entry: Property): Unit = {
    persist(PropertyService.PropertySetEvt(entry)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  def removeProperty(state: PropertyState, id: Property.Id): Unit = {
    if (state.propertyExists(id)) {
      persist(PropertyService.PropertyRemovedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! PropertyService.EntryNotFound
    }
  }

  def findPropertyById(state: PropertyState, id: Property.Id): Unit =
    sender ! PropertyService.PropertyOption(state.findPropertyById(id))

  def findAllProperties(state: PropertyState): Unit =
    sender ! PropertyService.PropertySeq(state.findAllProperties)

  def findProperties(
    state: PropertyState,
    userId: FilterOption[User.Id],
    tenantId: FilterOption[Tenant.Id],
    applicationId: FilterOption[Application.Id],
    languageId: FilterOption[Language.Id],
    key: FilterOption[String]): Unit = {
    sender ! PropertyService.PropertySeq(state.findProperties(userId, tenantId, applicationId, languageId, key))
  }

  def behavior(state: PropertyState): Receive = {
    case PropertyService.SetPropertyCmd(entry) => setProperty(state, entry)
    case PropertyService.RemovePropertyCmd(id) => removeProperty(state, id)
    case PropertyService.FindPropertyById(id) => findPropertyById(state, id)
    case PropertyService.FindProperties(userId, tenantId, applicationId, languageId, key) => findProperties(state, userId, tenantId, applicationId, languageId, key)
    case PropertyService.FindAllProperties => findAllProperties(state)

  }

}
