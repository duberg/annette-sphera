package annette.core.domain.property

import akka.Done
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.property.PropertyService._
import annette.core.domain.property.model._
import annette.core.domain.tenancy.model.{ Tenant, User }
import annette.core.utils.FilterOption

class PropertyActor(val initState: PropertyState = PropertyState()) extends CqrsPersistentActor[PropertyState] {

  def setProperty(state: PropertyState, entry: Property): Unit = {
    persist(PropertySetEvt(entry)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  def removeProperty(state: PropertyState, id: Property.Id): Unit = {
    if (state.propertyExists(id)) {
      persist(PropertyRemovedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! EntryNotFound
    }
  }

  def findPropertyById(state: PropertyState, id: Property.Id): Unit =
    sender ! PropertyOption(state.findPropertyById(id))

  def findAllProperties(state: PropertyState): Unit =
    sender ! PropertySeq(state.findAllProperties)

  def findProperties(
    state: PropertyState,
    userId: FilterOption[User.Id],
    tenantId: FilterOption[Tenant.Id],
    applicationId: FilterOption[Application.Id],
    languageId: FilterOption[Language.Id],
    key: FilterOption[String]): Unit = {
    sender ! PropertySeq(state.findProperties(userId, tenantId, applicationId, languageId, key))
  }

  def behavior(state: PropertyState): Receive = {
    case SetPropertyCmd(entry) => setProperty(state, entry)
    case RemovePropertyCmd(id) => removeProperty(state, id)
    case FindPropertyById(id) => findPropertyById(state, id)
    case FindProperties(userId, tenantId, applicationId, languageId, key) => findProperties(state, userId, tenantId, applicationId, languageId, key)
    case FindAllProperties => findAllProperties(state)

  }

}
