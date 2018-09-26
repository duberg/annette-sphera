package annette.core.model

import annette.core.akkaext.actor._
import annette.core.domain.tenancy.model.User

case class Modifier(actorId: Option[ActorId], userId: Option[User.Id], modifierType: ModifierType) {
  def isActor: Boolean = modifierType != ModifierType.User
}

object Modifier {
  def apply(actorId: ActorId, modifierType: ModifierType): Modifier = {
    new Modifier(Option(actorId), None, modifierType)
  }

  def apply(userId: User.Id): Modifier = {
    new Modifier(None, Option(userId), ModifierType.User)
  }
}
