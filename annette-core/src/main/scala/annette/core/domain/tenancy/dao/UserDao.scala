/**
 * *************************************************************************************
 * Copyright (c) 2014-2017 by Valery Lobachev
 * Redistribution and use in source and binary forms, with or without
 * modification, are NOT permitted without written permission from Valery Lobachev.
 *
 * Copyright (c) 2014-2017 Валерий Лобачев
 * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
 * запрещено без письменного разрешения правообладателя.
 * **************************************************************************************
 */

package annette.core.domain.tenancy.dao

import javax.inject._

import akka.Done
import akka.actor.ActorRef
import annette.core.domain.language.{ LanguageAlreadyExists, LanguageService }
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model.{ User, UserUpdate }
import annette.core.domain.tenancy.model.User.Id
import org.mindrot.jbcrypt.BCrypt
import akka.pattern.ask
import annette.core.exception.AnnetteMessage

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 18.12.16.
 */
@Singleton
class UserDao @Inject() (
  @Named("CoreService") actor: ActorRef) extends IUserDao {

  override def create(user: User, password: String)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, UserService.CreateUserCmd(user, password))
    } yield {
      f match {
        case Done =>
        case m: AnnetteMessage => throw m.toException

      }
    }
  }

  override def update(user: UserUpdate)(implicit ec: ExecutionContext): Future[Unit] = {
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

