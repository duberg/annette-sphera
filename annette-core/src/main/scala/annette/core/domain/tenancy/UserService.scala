package annette.core.domain.tenancy

import akka.Done
import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import annette.core.domain.tenancy.model.User.Id
import annette.core.domain.tenancy.model._
import annette.core.exception.AnnetteMessage
import annette.core.persistence.Persistence.{PersistentCommand, PersistentEvent, PersistentQuery}
import javax.inject.{Inject, Named, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.domain.tenancy.actor.{UsersActor, UsersActorState}
import annette.core.domain.tenancy.model.User.Id
import annette.core.domain.tenancy.model.{User, UpdateUser}
import annette.core.exception.AnnetteMessage
import javax.inject._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(@Named("CoreService") actor: ActorRef)(implicit c: ExecutionContext, t: Timeout) {
  def create(user: User, password: String): Future[Unit] = {
    for {
      f <- ask(actor, UserService.CreateUserCmd(user, password))
    } yield {
      f match {
        case Done =>
        case m: AnnetteMessage => throw m.toException

      }
    }
  }

  override def update(user: UpdateUser)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, UserService.UpdateUserCmd(user))
    } yield {
      f match {
        case Done =>
        case m: AnnetteMessage => throw m.toException

      }
    }
  }

  override def setPassword(userId: Id, password: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    for {
      f <- ask(actor, UserService.UpdatePasswordCmd(userId, password))
    } yield {
      f match {
        case Done => true
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  override def delete(id: Id)(implicit ec: ExecutionContext): Future[Boolean] = {
    for {
      f <- ask(actor, UserService.DeleteUserCmd(id))
    } yield {
      f match {
        case Done => true
        case m: AnnetteMessage => throw m.toException
      }
    }
  }

  override def getById(id: Id)(implicit ec: ExecutionContext): Future[Option[User]] = {
    ask(actor, UserService.FindUserById(id)).mapTo[UserService.SingleUser].map(_.maybeEntry)
  }

  override def selectAll(implicit ec: ExecutionContext): Future[List[User]] = {
    ask(actor, UserService.FindAllUsers).mapTo[UserService.MultipleUsers].map(_.entries.values.toList)
  }

  override def getByLoginAndPassword(login: String, password: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    ask(actor, UserService.FindUserByLoginAndPassword(login, password)).mapTo[UserService.SingleUser].map(_.maybeEntry)
  }
}

object UserService {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

  case class CreateUserCmd(x: User) extends Command
  case class UpdateUserCmd(x: UpdateUser) extends Command
  case class DeleteUserCmd(userId: User.Id) extends Command

  case class UpdatePasswordCmd(userId: User.Id, password: String) extends Command
  case class FindUserById(id: User.Id) extends Query
  case class FindUserByLoginAndPassword(login: String, password: String) extends Query
  object FindAllUsers extends Query

  case class UserCreatedEvt(entry: User, password: String) extends Event
  case class UserUpdatedEvt(entry: UpdateUser) extends Event
  case class PasswordUpdatedEvt(userId: User.Id, password: String) extends Event
  case class UserDeletedEvt(id: User.Id) extends Event

  case class SingleUser(maybeEntry: Option[User]) extends Response
  case class MultipleUsers(entries: Map[User.Id, User]) extends Response

  def props(id: String, state: UsersActorState = UsersActorState()) = Props(classOf[UsersActor], id, state)
}
