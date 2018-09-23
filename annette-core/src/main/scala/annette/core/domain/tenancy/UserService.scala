package annette.core.domain.tenancy

import akka.Done
import akka.actor.{ ActorRef, Props }
import akka.util.Timeout
import annette.core.domain.tenancy.model.User.Id
import annette.core.domain.tenancy.model._
import annette.core.exception.AnnetteMessage
import annette.core.persistence.Persistence.{ PersistentCommand, PersistentEvent, PersistentQuery }
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.{ ExecutionContext, Future }
import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.domain.tenancy.UserService.{ CreateUserSuccess, CreatedUserEvt }
import annette.core.domain.tenancy.actor.{ UsersActor, UsersState }
import annette.core.domain.tenancy.model.User.Id
import annette.core.domain.tenancy.model.{ UpdateUser, User }
import annette.core.exception.AnnetteMessage
import javax.inject._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

@Singleton
class UserService @Inject() (@Named("CoreService") actor: ActorRef) {
  implicit def t: Timeout = 3.minutes

  def create(x: CreateUser)(implicit ec: ExecutionContext): Future[User] = {
    for {
      f <- ask(actor, UserService.CreateUserCmd(x))
    } yield {
      f match {
        case CreateUserSuccess(u) => u
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  def update(x: UpdateUser)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, UserService.UpdateUserCmd(x))
    } yield {
      f match {
        case Done => println("--ss---")
        case m: AnnetteMessage => throw m.toException

      }
    }
  }

  def setPassword(userId: Id, password: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    for {
      f <- ask(actor, UserService.UpdatePasswordCmd(userId, password))
    } yield {
      f match {
        case Done => true
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  def delete(userId: Id)(implicit ec: ExecutionContext): Future[Boolean] = {
    for {
      f <- ask(actor, UserService.DeleteUserCmd(userId))
    } yield {
      f match {
        case Done => true
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  def getById(id: Id)(implicit ec: ExecutionContext): Future[Option[User]] = {
    ask(actor, UserService.FindUserById(id)).mapTo[UserService.SingleUser].map(_.maybeEntry)
  }

  def selectAll(implicit ec: ExecutionContext): Future[List[User]] = {
    ask(actor, UserService.FindAllUsers).mapTo[UserService.MultipleUsers].map(_.entries.values.toList)
  }

  def getByLoginAndPassword(login: String, password: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    ask(actor, UserService.FindUserByLoginAndPassword(login, password)).mapTo[UserService.SingleUser].map(_.maybeEntry)
  }
}

object UserService {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

  case class CreateUserCmd(x: CreateUser) extends Command
  case class UpdateUserCmd(x: UpdateUser) extends Command
  case class DeleteUserCmd(userId: User.Id) extends Command

  case class UpdatePasswordCmd(userId: User.Id, password: String) extends Command
  case class FindUserById(id: User.Id) extends Query
  case class FindUserByLoginAndPassword(login: String, password: String) extends Query
  object FindAllUsers extends Query

  case class CreatedUserEvt(x: User) extends Event
  case class UpdatedUserEvt(x: UpdateUser) extends Event
  case class UpdatedPasswordEvt(userId: User.Id, password: String) extends Event
  case class DeletedUserEvt(userId: User.Id) extends Event

  case class CreateUserSuccess(x: User) extends Response
  case class SingleUser(maybeEntry: Option[User]) extends Response
  case class MultipleUsers(entries: Map[User.Id, User]) extends Response

  def props(id: String, state: UsersState = UsersState()) = Props(classOf[UsersActor], id, state)
}
