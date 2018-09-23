package annette.core.domain.tenancy.actor

import akka.Done
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.{UserNotFoundMsg, UserService}
import annette.core.exception.AnnetteMessageException
import annette.core.persistence.Persistence._

import scala.util.Try

class UsersActor(val id: String, val initState: UsersActorState) extends PersistentStateActor[UsersActorState] {

  def processFailure: PartialFunction[Throwable, Unit] = {
    case e: AnnetteMessageException =>
      sender ! e.message
    case th: Throwable =>
      sender ! akka.actor.Status.Failure(th)
  }

  def createUser(state: UsersActorState, entry: User, password: String): Unit = {
    val validateResult = Try { state.validateCreate(entry) }
    validateResult.fold(
      processFailure,
      _ =>
        persist(UserService.UserCreatedEvt(entry: User, password: String)) { event =>
          changeState(state.updated(event))
          sender ! Done
        })
  }

  def updateUser(state: UsersActorState, entry: UpdateUser): Unit = {
    val validateResult = Try {
      state.validateUpdate(entry)
    }
    validateResult.fold(
      processFailure,
      _ =>
        persist(UserService.UserUpdatedEvt(entry)) { event =>
          changeState(state.updated(event))
          sender ! Done
        })
  }

  def deleteUser(state: UsersActorState, id: User.Id): Unit = {
    if (state.userExists(id)) {
      persist(UserService.UserDeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! UserNotFoundMsg(id)
    }
  }

  def findUserById(state: UsersActorState, id: User.Id): Unit =
    sender ! UserService.SingleUser(state.findUserById(id))

  def findAllUsers(state: UsersActorState): Unit =
    sender ! UserService.MultipleUsers(state.findAllUsers)

  def updatePassword(state: UsersActorState, userId: User.Id, password: String): Unit = {
    if (state.userExists(userId)) {
      persist(UserService.PasswordUpdatedEvt(userId, password)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! UserNotFoundMsg(userId)
    }
  }

  def findUserByLoginAndPassword(state: UsersActorState, login: String, password: String): Unit = {
    sender ! UserService.SingleUser(state.findUserByLoginAndPassword(login, password))
  }

  def behavior(state: UsersActorState): Receive = {
    case UserService.CreateUserCmd(entry, password) => createUser(state, entry, password)
    case UserService.UpdateUserCmd(entry) => updateUser(state, entry)
    case UserService.DeleteUserCmd(id) => deleteUser(state, id)
    case UserService.FindUserById(id) => findUserById(state, id)
    case UserService.FindAllUsers => findAllUsers(state)

    case UserService.UpdatePasswordCmd(userId, password) => updatePassword(state, userId, password)
    case UserService.FindUserByLoginAndPassword(login, password) => findUserByLoginAndPassword(state, login, password)

  }

}
