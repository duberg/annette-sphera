package annette.imc.user

import java.util.UUID

import annette.core.domain.tenancy.model.User
import annette.core.persistence.Persistence.{ PersistentEvent, PersistentState }
import annette.imc.user.model.ImcUser

case class ImcUserState(storage: Map[UUID, ImcUser] = Map.empty) extends PersistentState[ImcUserState] {

  def create(c: ImcUser) = {
    if (storage.get(c.id).isDefined) throw new IllegalArgumentException
    copy(
      storage + (c.id -> ImcUser(
        id = c.id,
        sphere = c.sphere,
        company = c.company,
        position = c.position,
        rank = c.rank,
        otherTel = c.otherTel,
        otherMail = c.otherMail)))
  }

  def update(c: ImcUser) = {
    if (storage.get(c.id).isEmpty) throw new IllegalArgumentException
    val s = storage(c.id)
    copy(
      storage + (c.id -> ImcUser(
        id = c.id,
        sphere = c.sphere,
        company = c.company,
        position = c.position,
        rank = c.rank,
        otherTel = c.otherTel,
        otherMail = c.otherMail)))
  }

  def delete(id: User.Id) = {
    ImcUserState(storage - id)
  }

  def resetUpdateCounter = {
    ImcUserState(storage)
  }

  def exists(id: User.Id) = storage.get(id).isDefined
  def getById(id: User.Id) = storage.get(id)
  def getAll = storage
  //  def clear(): CharacteristicState = copy(storage = Map.empty)

  override def updated(event: PersistentEvent): ImcUserState = event match {
    case ImcUserActor.CreatedEvt(x) => create(x)
    case ImcUserActor.UpdatedEvt(x) => update(x)
    case ImcUserActor.DeletedEvt(x) => delete(x)

  }

}

