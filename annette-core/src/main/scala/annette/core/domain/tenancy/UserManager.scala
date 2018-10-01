package annette.core.domain.tenancy

import akka.Done
import akka.actor.{ ActorRef, Props }
import akka.util.Timeout
import annette.core.domain.tenancy.model.User.Id
import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence.{ PersistentCommand, PersistentEvent, PersistentQuery }
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.{ ExecutionContext, Future }
import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.AnnetteMessage
import annette.core.akkaext.http.PageRequest
import annette.core.domain.tenancy.UserManager.{ CreateUserSuccess, CreatedUserEvt }
import annette.core.domain.tenancy.actor.{ UsersActor, UsersState }
import annette.core.domain.tenancy.model.User.Id
import annette.core.domain.tenancy.model.{ UpdateUser, User }
import annette.core.security.verification.VerificationBus
import javax.inject._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

@Singleton
class UserManager @Inject() (@Named("CoreService") actor: ActorRef) {
  implicit def t: Timeout = 3.minutes

  def create(x: CreateUser)(implicit ec: ExecutionContext): Future[User] = {
    for {
      f <- ask(actor, UserManager.CreateUserCmd(x))
    } yield {
      f match {
        case CreateUserSuccess(u) => u
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  def update(x: UpdateUser)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, UserManager.UpdateUserCmd(x))
    } yield {
      f match {
        case Done => println("--ss---")
        case m: AnnetteMessage => throw m.toException

      }
    }
  }

  def setPassword(userId: Id, password: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    for {
      f <- ask(actor, UserManager.UpdatePasswordCmd(userId, password))
    } yield {
      f match {
        case Done => true
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  def delete(userId: Id)(implicit ec: ExecutionContext): Future[Boolean] = {
    for {
      f <- ask(actor, UserManager.DeleteUserCmd(userId))
    } yield {
      f match {
        case Done => true
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  def getById(id: Id)(implicit ec: ExecutionContext): Future[Option[User]] = {
    ask(actor, UserManager.GetUserById(id)).mapTo[UserManager.UserOpt].map(_.maybeEntry)
  }

  def listUsers(implicit ec: ExecutionContext): Future[List[User]] = {
    ask(actor, UserManager.ListUsers).mapTo[UserManager.UsersMap].map(_.x.values.toList)
  }

  def getByLoginAndPassword(login: String, password: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    ask(actor, UserManager.GetUserByLoginAndPassword(login, password)).mapTo[UserManager.UserOpt].map(_.maybeEntry)
  }
}

object UserManager {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

  case class CreateUserCmd(x: CreateUser) extends Command
  case class UpdateUserCmd(x: UpdateUser) extends Command
  case class DeleteUserCmd(userId: User.Id) extends Command
  case class UpdatePasswordCmd(userId: User.Id, password: String) extends Command

  case class GetUserById(id: User.Id) extends Query
  case class GetUserByLoginAndPassword(login: String, password: String) extends Query
  object ListUsers extends Query
  case class PaginateListUsers(page: PageRequest) extends Query

  case class CreatedUserEvt(x: User) extends Event
  case class UpdatedUserEvt(x: UpdateUser) extends Event
  case class UpdatedPasswordEvt(userId: User.Id, password: String) extends Event
  case class DeletedUserEvt(userId: User.Id) extends Event
  case class ActivatedUserEvt(userId: User.Id) extends Event

  case class CreateUserSuccess(x: User) extends Response
  case class UserOpt(maybeEntry: Option[User]) extends Response
  case class UsersMap(x: Map[User.Id, User]) extends Response

  def props(id: String, verificationBus: VerificationBus, state: UsersState = UsersState()) =
    Props(new UsersActor(
      id = id,
      verificationBus = verificationBus,
      initState = state))
}
