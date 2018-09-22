package annette.core.domain.tenancy

import akka.actor.Props
import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence.{ PersistentCommand, PersistentEvent, PersistentQuery }

object UserService {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

  case class CreateUserCmd(entry: User, password: String) extends Command
  case class UpdateUserCmd(entry: UserUpdate) extends Command
  case class DeleteUserCmd(id: User.Id) extends Command
  case class UpdatePasswordCmd(userId: User.Id, password: String) extends Command
  case class FindUserById(id: User.Id) extends Query
  case class FindUserByLoginAndPassword(login: String, password: String) extends Query
  object FindAllUsers extends Query

  case class UserCreatedEvt(entry: User, password: String) extends Event
  case class UserUpdatedEvt(entry: UserUpdate) extends Event
  case class PasswordUpdatedEvt(userId: User.Id, password: String) extends Event
  case class UserDeletedEvt(id: User.Id) extends Event

  case class SingleUser(maybeEntry: Option[User]) extends Response
  case class MultipleUsers(entries: Map[User.Id, User]) extends Response

  def props(id: String, state: UserState = UserState()) = Props(classOf[UserActor], id, state)
}
