package annette.core.domain.property

import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.{ Tenant, User }
import annette.core.persistence.Persistence
import annette.core.persistence.Persistence.PersistentState
import annette.core.domain.property.model._
import annette.core.utils.{ AnyValue, FilterOption, NoValue, Value }

case class PropertyState(
  properties: Map[Property.Id, Property] = Map.empty,
  userIndex: Map[Option[User.Id], Set[Property.Id]] = Map.empty,
  tenantIndex: Map[Option[Tenant.Id], Set[Property.Id]] = Map.empty,
  applicationIndex: Map[Option[Application.Id], Set[Property.Id]] = Map.empty,
  languageIndex: Map[Option[Language.Id], Set[Property.Id]] = Map.empty,
  keyIndex: Map[Option[String], Set[Property.Id]] = Map.empty) extends PersistentState[PropertyState] {

  def setProperty(entry: Property): PropertyState = {
    copy(
      properties = properties + (entry.id -> entry),
      userIndex = addToIndex(userIndex, entry.userId, entry.id),
      tenantIndex = addToIndex(tenantIndex, entry.tenantId, entry.id),
      applicationIndex = addToIndex(applicationIndex, entry.applicationId, entry.id),
      languageIndex = addToIndex(languageIndex, entry.languageId, entry.id),
      keyIndex = addToIndex(keyIndex, Some(entry.key), entry.id))
  }

  private def addToIndex[T](index: Map[T, Set[Property.Id]], indexId: T, propertyId: Property.Id): Map[T, Set[Property.Id]] = {
    val newSet = index.getOrElse(indexId, Set.empty) + propertyId
    index + (indexId -> newSet)
  }

  private def removeFromIndex[T](index: Map[T, Set[Property.Id]], indexId: T, propertyId: Property.Id): Map[T, Set[Property.Id]] = {
    val newSet = index.getOrElse(indexId, Set.empty) - propertyId
    if (newSet.isEmpty) index - indexId
    else index + (indexId -> newSet)
  }

  def removeProperty(id: Property.Id): PropertyState = {
    properties.get(id).map {
      entry =>
        copy(
          properties = properties - id,
          userIndex = removeFromIndex(userIndex, entry.userId, entry.id),
          tenantIndex = removeFromIndex(tenantIndex, entry.tenantId, entry.id),
          applicationIndex = removeFromIndex(applicationIndex, entry.applicationId, entry.id),
          languageIndex = removeFromIndex(languageIndex, entry.languageId, entry.id),
          keyIndex = removeFromIndex(keyIndex, Some(entry.key), entry.id))
    }.getOrElse(throw new IllegalArgumentException)
  }

  def findPropertyById(id: Property.Id): Option[Property] = properties.get(id)

  def findProperties(
    userId: FilterOption[User.Id],
    tenantId: FilterOption[Tenant.Id],
    applicationId: FilterOption[Application.Id],
    languageId: FilterOption[Language.Id],
    key: FilterOption[String]): Seq[Property] = {
    val ids = Seq(
      getIndexIds(userIndex, userId),
      getIndexIds(tenantIndex, tenantId),
      getIndexIds(applicationIndex, applicationId),
      getIndexIds(languageIndex, languageId),
      getIndexIds(keyIndex, key)).flatten.reduce((left, right) => left & right)
    ids.flatMap(k => properties.get(k)).toSeq
  }

  def getIndexIds[T](index: Map[Option[T], Set[Property.Id]], indexFilter: FilterOption[T]): Option[Set[Property.Id]] = {
    indexFilter match {
      case Value(a) => Some(index.getOrElse(Some(a), Set.empty))
      case AnyValue => None
      case NoValue => Some(index.getOrElse(None, Set.empty))
    }
  }

  def findAllProperties: Seq[Property] = properties.values.toSeq

  def propertyExists(id: Property.Id): Boolean = properties.get(id).isDefined

  override def updated(event: Persistence.PersistentEvent) = {
    event match {
      case PropertyService.PropertySetEvt(entry) => setProperty(entry)
      case PropertyService.PropertyRemovedEvt(id) => removeProperty(id)
    }
  }
}