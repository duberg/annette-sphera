package annette.core.domain.tenancy.actor

import java.time.ZonedDateTime
import java.util.UUID

import akka.Done
import annette.core.AnnetteMessageException
import annette.core.akkaext.http.PageRequest
import annette.core.akkaext.http.Pagination.paginate
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.UserNotFoundMsg
import annette.core.domain.tenancy.model.User._
import annette.core.domain.tenancy.model._
import annette.core.security.verification._
import org.mindrot.jbcrypt.BCrypt

import scala.util.Try

class UserManagerActor(val verificationBus: VerificationBus, val initState: UserManagerState = UserManagerState()) extends CqrsPersistentActor[UserManagerState] {
  def processFailure: PartialFunction[Throwable, Unit] = {
    case e: AnnetteMessageException =>
      sender ! e.message
    case th: Throwable =>
      sender ! akka.actor.Status.Failure(th)
  }

  def createUser(state: UserManagerState, x: CreateUser): Unit = {
    val validateResult = Try { state.validateCreate(x) }
    validateResult.fold(processFailure, _ => {
      val userId = x.id.getOrElse(UUID.randomUUID())
      val hashedPassword = BCrypt.hashpw(x.password, BCrypt.gensalt())
      val user = User(
        id = userId,
        username = x.username,
        displayName = x.displayName,
        firstName = x.firstName,
        lastName = x.lastName,
        middleName = x.middleName,
        gender = x.gender,
        email = x.email,
        url = x.url,
        description = x.description,
        phone = x.phone,
        language = x.language,
        registeredDate = ZonedDateTime.now(),
        roles = x.roles.getOrElse(Map.empty),
        password = hashedPassword,
        avatarUrl = x.avatarUrl,
        sphere = x.sphere,
        company = x.company,
        position = x.position,
        rank = x.rank,
        additionalTel = x.additionalTel,
        additionalMail = x.additionalMail,
        meta = x.meta.getOrElse(Map.empty),
        status = x.status.getOrElse(0))

      persist(CreatedUserEvt(user)) { event =>
        changeState(state.updated(event))
        sender ! CreateUserSuccess(user)
      }
    })
  }

  def updateUser(state: UserManagerState, entry: UpdateUser): Unit = {
    val validateResult = Try {
      state.validateUpdate(entry)
    }
    validateResult.fold(
      processFailure,
      _ =>
        persist(UpdatedUserEvt(entry)) { event =>
          changeState(state.updated(event))
          sender ! Done
        })
  }

  def deleteUser(state: UserManagerState, id: User.Id): Unit = {
    if (state.userExists(id)) {
      persist(DeletedUserEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! UserNotFoundMsg(id)
    }
  }

  def findUserById(state: UserManagerState, id: User.Id): Unit =
    sender ! UserOpt(state.findUserById(id))

  def listUsers(state: UserManagerState): Unit =
    sender ! UsersMap(state.users)

  def paginateUsers(state: UserManagerState, page: PageRequest): Unit = {
    val (items, totalCount) = paginate(state.users, page)
    sender ! UsersList(PaginateUsers(items, totalCount))
  }

  def updatePassword(state: UserManagerState, userId: User.Id, password: String): Unit = {
    if (state.userExists(userId)) {
      persist(UpdatedPasswordEvt(userId, password)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! UserNotFoundMsg(userId)
    }
  }

  def findUserByLoginAndPassword(state: UserManagerState, login: String, password: String): Unit = {
    sender ! UserOpt(state.findUserByLoginAndPassword(login, password))
  }

  def activateUser(state: UserManagerState, email: String): Unit = {
    state.findUserByEmail(email).foreach { x =>
      persist(ActivatedUserEvt(x.id)) { event =>
        changeState(state.updated(event))
      }
    }
  }

  def behavior(state: UserManagerState): Receive = {
    case CreateUserCmd(x) => createUser(state, x)
    case UpdateUserCmd(x) => updateUser(state, x)
    case DeleteUserCmd(x) => deleteUser(state, x)
    case GetUserById(x) => findUserById(state, x)
    case ListUsers => listUsers(state)
    case PaginateListUsers(x) => paginateUsers(state, x)
    case UpdatePasswordCmd(userId, password) => updatePassword(state, userId, password)
    case GetUserByLoginAndPassword(login, password) => findUserByLoginAndPassword(state, login, password)
    case Verification.EmailVerifiedEvt(x) => activateUser(state, x.email)
  }

  override def afterRecover(state: UserManagerState): Unit = {
    verificationBus.subscribe(self, "email")
  }
}
