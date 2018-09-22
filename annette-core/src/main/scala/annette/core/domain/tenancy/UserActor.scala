package annette.core.domain.tenancy

import akka.Done
import annette.core.domain.tenancy.model._
import annette.core.exception.AnnetteMessageException
import annette.core.persistence.Persistence._

import scala.util.Try

class UserActor(val id: String, val initState: UserState) extends PersistentStateActor[UserState] {

  def processFailure: PartialFunction[Throwable, Unit] = {
    case e: AnnetteMessageException =>
      sender ! e.message
    case th: Throwable =>
      sender ! akka.actor.Status.Failure(th)
  }

  def createUser(state: UserState, entry: User, password: String): Unit = {
    log.info("createUser")

    val validateResult = Try { state.validateCreate(entry) }
    validateResult.fold(
      processFailure,
      _ =>
        persist(UserService.UserCreatedEvt(entry: User, password: String)) { event =>
          changeState(state.updated(event))
          sender ! Done
        })
  }

  def updateUser(state: UserState, entry: UserUpdate): Unit = {
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

  def deleteUser(state: UserState, id: User.Id): Unit = {
    if (state.userExists(id)) {
      persist(UserService.UserDeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! UserNotFoundMsg(id)
    }
  }

  def findUserById(state: UserState, id: User.Id): Unit =
    sender ! UserService.SingleUser(state.findUserById(id))

  def findAllUsers(state: UserState): Unit =
    sender ! UserService.MultipleUsers(state.findAllUsers)

  def updatePassword(state: UserState, userId: User.Id, password: String): Unit = {
    if (state.userExists(userId)) {
      persist(UserService.PasswordUpdatedEvt(userId, password)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! UserNotFoundMsg(userId)
    }
  }

  def findUserByLoginAndPassword(state: UserState, login: String, password: String): Unit = {
    sender ! UserService.SingleUser(state.findUserByLoginAndPassword(login, password))
  }

  def behavior(state: UserState): Receive = {
    case UserService.CreateUserCmd(entry, password) => createUser(state, entry, password)
    case UserService.UpdateUserCmd(entry) => updateUser(state, entry)
    case UserService.DeleteUserCmd(id) => deleteUser(state, id)
    case UserService.FindUserById(id) => findUserById(state, id)
    case UserService.FindAllUsers => findAllUsers(state)

    case UserService.UpdatePasswordCmd(userId, password) => updatePassword(state, userId, password)
    case UserService.FindUserByLoginAndPassword(login, password) => findUserByLoginAndPassword(state, login, password)

  }

}
