package annette.core.domain.tenancy.actor

import java.time.ZonedDateTime
import java.util.UUID

import akka.Done
import annette.core.AnnetteMessageException
import annette.core.domain.tenancy.UserService.CreateUserSuccess
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.{ UserNotFoundMsg, UserService }
import annette.core.persistence.Persistence._
import com.outworkers.phantom.builder.QueryBuilder.Create
import org.mindrot.jbcrypt.BCrypt

import scala.util.Try

class UsersActor(val id: String, val initState: UsersState) extends PersistentStateActor[UsersState] {

  def processFailure: PartialFunction[Throwable, Unit] = {
    case e: AnnetteMessageException =>
      sender ! e.message
    case th: Throwable =>
      sender ! akka.actor.Status.Failure(th)
  }

  def createUser(state: UsersState, x: CreateUser): Unit = {
    val validateResult = Try { state.validateCreate(x) }
    validateResult.fold(processFailure, _ => {
      val userId = UUID.randomUUID()
      val hashedPassword = BCrypt.hashpw(x.password, BCrypt.gensalt())
      val user = User(
        id = userId,
        username = x.username,
        displayName = x.displayName,
        firstName = x.firstName,
        lastName = x.lastName,
        middleName = x.middleName,
        email = x.email,
        url = x.url,
        description = x.description,
        phone = x.phone,
        language = x.language,
        registeredDate = ZonedDateTime.now(),
        //tenants = x.tenants,
        //applications = x.applications,
        //roles = x.roles,
        password = hashedPassword,
        avatarUrl = x.avatarUrl,
        sphere = x.sphere,
        company = x.company,
        position = x.position,
        rank = x.rank,
        additionalTel = x.additionalTel,
        additionalMail = x.additionalMail,
        meta = x.meta,
        deactivated = x.deactivated)

      persist(UserService.CreatedUserEvt(user)) { event =>
        changeState(state.updated(event))
        sender ! UserService.CreateUserSuccess(user)
      }
    })
  }

  def updateUser(state: UsersState, entry: UpdateUser): Unit = {
    val validateResult = Try {
      state.validateUpdate(entry)
    }
    validateResult.fold(
      processFailure,
      _ =>
        persist(UserService.UpdatedUserEvt(entry)) { event =>
          changeState(state.updated(event))
          sender ! Done
        })
  }

  def deleteUser(state: UsersState, id: User.Id): Unit = {
    if (state.userExists(id)) {
      persist(UserService.DeletedUserEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! UserNotFoundMsg(id)
    }
  }

  def findUserById(state: UsersState, id: User.Id): Unit =
    sender ! UserService.SingleUser(state.findUserById(id))

  def findAllUsers(state: UsersState): Unit =
    sender ! UserService.MultipleUsers(state.users)

  def updatePassword(state: UsersState, userId: User.Id, password: String): Unit = {
    if (state.userExists(userId)) {
      persist(UserService.UpdatedPasswordEvt(userId, password)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! UserNotFoundMsg(userId)
    }
  }

  def findUserByLoginAndPassword(state: UsersState, login: String, password: String): Unit = {
    sender ! UserService.SingleUser(state.findUserByLoginAndPassword(login, password))
  }

  def behavior(state: UsersState): Receive = {
    case UserService.CreateUserCmd(x) => createUser(state, x)
    case UserService.UpdateUserCmd(x) => updateUser(state, x)
    case UserService.DeleteUserCmd(x) => deleteUser(state, x)
    case UserService.FindUserById(x) => findUserById(state, x)
    case UserService.FindAllUsers => findAllUsers(state)

    case UserService.UpdatePasswordCmd(userId, password) => updatePassword(state, userId, password)
    case UserService.FindUserByLoginAndPassword(login, password) => findUserByLoginAndPassword(state, login, password)

  }

}
