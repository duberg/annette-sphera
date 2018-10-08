package annette.core.domain.tenancy

import akka.Done
import akka.actor.{ ActorRef, Props }
import akka.util.Timeout
import annette.core.domain.tenancy.model.User.Id
import annette.core.domain.tenancy.model._
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.{ ExecutionContext, Future }
import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.AnnetteMessage
import annette.core.akkaext.actor._
import annette.core.akkaext.http.PageRequest
import annette.core.domain.tenancy.model.User._
import annette.core.domain.tenancy.actor.{ UserManagerActor, UserManagerState }
import annette.core.domain.tenancy.model.User.Id
import annette.core.domain.tenancy.model.{ UpdateUser, User }
import annette.core.security.verification.VerificationBus
import javax.inject._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import annette.core.domain.tenancy.model.User._

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

  def getById(x: User.Id): Future[Option[User]] =
    ask(actor, GetUserById(x))
      .mapTo[UserOpt]
      .map(_.maybeEntry)

  def listTenantsIds(x: User.Id): Future[Set[Tenant.Id]] =
    getById(x).map(_.map(_.roles.keys.toSet).getOrElse(Set.empty))

  def listUsers: Future[List[User]] =
    ask(actor, ListUsers)
      .mapTo[UsersMap]
      .map(_.x.values.toList)

  def paginateListUsers(page: PageRequest): Future[PaginateUsersList] =
    ask(actor, PaginateListUsers(page))
      .mapTo[UsersList]
      .map(_.x)

  def getByLoginAndPassword(login: String, password: String): Future[Option[User]] =
    ask(actor, GetUserByLoginAndPassword(login, password))
      .mapTo[UserOpt]
      .map(_.maybeEntry)
}

object UserManager {
  def props(id: ActorId, verificationBus: VerificationBus, state: UserManagerState = UserManagerState()) =
    Props(new UserManagerActor(
      id = id,
      verificationBus = verificationBus,
      initState = state))
}
