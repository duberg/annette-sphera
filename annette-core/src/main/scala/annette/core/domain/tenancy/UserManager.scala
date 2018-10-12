package annette.core.domain.tenancy

import akka.Done
import akka.actor.{ ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import annette.core.AnnetteMessage
import annette.core.akkaext.http.PageRequest
import annette.core.domain.tenancy.actor.{ UserManagerActor, UserManagerState }
import annette.core.domain.tenancy.model.User.{ Id, _ }
import annette.core.domain.tenancy.model.{ UpdateUser, User, _ }
import annette.core.security.verification.VerificationBus
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class UserManager @Inject() (@Named("CoreService") actor: ActorRef)(implicit c: ExecutionContext, t: Timeout) {
  def create(x: CreateUser): Future[User] =
    ask(actor, CreateUserCmd(x))
      // .mapTo[Response]
      .map {
        case CreateUserSuccess(y) => y
        case m: AnnetteMessage => throw m.toException
      }

  def update(x: UpdateUser): Future[Unit] = {
    for {
      f <- ask(actor, UpdateUserCmd(x))
    } yield {
      f match {
        case Done =>
        case m: AnnetteMessage => throw m.toException

      }
    }
  }

  def setPassword(userId: Id, password: String): Future[Boolean] = {
    for {
      f <- ask(actor, UpdatePasswordCmd(userId, password))
    } yield {
      f match {
        case Done => true
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  def delete(userId: Id): Future[Boolean] = {
    for {
      f <- ask(actor, DeleteUserCmd(userId))
    } yield {
      f match {
        case Done => true
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  def getUserById(x: User.Id): Future[Option[User]] =
    ask(actor, GetUserById(x))
      .mapTo[UserOpt]
      .map(_.maybeEntry)

  def listUsers: Future[List[User]] =
    ask(actor, ListUsers)
      .mapTo[UsersMap]
      .map(_.x.values.toList)

  def paginateListUsers(page: PageRequest): Future[PaginateUsers] =
    ask(actor, PaginateListUsers(page))
      .mapTo[UsersList]
      .map(_.x)

  def getByLoginAndPassword(login: String, password: String): Future[Option[User]] =
    ask(actor, GetUserByLoginAndPassword(login, password))
      .mapTo[UserOpt]
      .map(_.maybeEntry)
}

object UserManager {
  def props(verificationBus: VerificationBus, state: UserManagerState = UserManagerState()) =
    Props(new UserManagerActor(verificationBus = verificationBus))
}
