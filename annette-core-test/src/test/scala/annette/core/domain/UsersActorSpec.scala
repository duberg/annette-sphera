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
package annette.core.domain

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.tenancy.UserManager.CreateUserSuccess
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model._
import annette.core.test.PersistenceSpec

class UsersActorSpec extends TestKit(ActorSystem("UserActorSpec"))
  with PersistenceSpec with NewUser {

  "A UserActor" when receive {
    "CreateUserCmd" must {
      "create new user" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserManager.CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc2 <- ask(actor, UserManager.CreateUserCmd(c2)).mapTo[CreateUserSuccess].map(_.x)
          ccs <- ask(actor, UserManager.FindAllUsers).mapTo[UserManager.MultipleUsers].map(_.entries)
        } yield {
          ccs(cc1.id) shouldBe a[User]
          ccs(cc2.id) shouldBe a[User]
        }
      }

      "should not create new user if there are no email & phone & login" in {
        val c1 = newCreateUser()
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserManager.CreateUserCmd(c1))
        } yield {
          cc1 shouldBe a[LoginRequiredMsg]
        }
      }

      //      "should not create new user if it already exists" in {
      //        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
      //        val c2 = c1.copy(email = Some("valery1@valery.com"), phone = Some("+7123451"), username = Some("valery1"))
      //        val actor = newUserActor()
      //        for {
      //
      //          cc1 <- ask(actor, UserService.CreateUserCmd(c1))
      //          cc2 <- ask(actor, UserService.CreateUserCmd(c2))
      //        } yield cc2 shouldBe a[UserAlreadyExistsMsg]
      //      }
      "should not create new user if email already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = c1.email)
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserManager.CreateUserCmd(c1))
          cc2 <- ask(actor, UserManager.CreateUserCmd(c2))
        } yield cc2 shouldBe a[EmailAlreadyExistsMsg]
      }
      "should not create new user if phone already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(phone = c1.phone)
        val actor = newUserActor()
        for {

          cc1 <- ask(actor, UserManager.CreateUserCmd(c1))
          cc2 <- ask(actor, UserManager.CreateUserCmd(c2))
        } yield cc2 shouldBe a[PhoneAlreadyExistsMsg]
      }
      "should not create new user if login already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(login = c1.username)
        val actor = newUserActor()
        for {

          cc1 <- ask(actor, UserManager.CreateUserCmd(c1))
          cc2 <- ask(actor, UserManager.CreateUserCmd(c2))
        } yield cc2 shouldBe a[LoginAlreadyExistsMsg]
      }

    }

    "UpdateUserCmd" must {
      "update all data of user" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))

        val actor = newUserActor()

        for {
          cc1 <- ask(actor, UserManager.CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc2 <- ask(actor, UserManager.UpdateUserCmd(
            UpdateUser(
              id = cc1.id,
              username = Some(c2.username),
              displayName = None,
              firstName = Some(c2.firstName),
              lastName = Some(c2.lastName),
              middleName = Some(c2.middleName),
              email = None,
              url = None,
              description = None,
              phone = None,
              language = Some(c2.language),
              //tenants = None,
              //applications = None,
              //roles = None,
              password = Some(c2.password),
              avatarUrl = None,
              sphere = None,
              company = None,
              position = None,
              rank = None,
              additionalTel = None,
              additionalMail = None,
              meta = None,
              status = None)))
          ccs <- ask(actor, UserManager.FindUserById(cc1.id)).mapTo[UserManager.SingleUser].map(_.maybeEntry.get)
        } yield {
          ccs shouldBe a[User]
        }
      }

      "should not update user if there are no email & phone & login" in {
        val c2 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))

        val actor = newUserActor()

        val f1 = ask(actor, UserManager.CreateUserCmd(c2))
          .mapTo[CreateUserSuccess]
          .map(_.x)
          .map(user => {
            UpdateUser(
              id = user.id,
              username = Some(None),
              displayName = None,
              firstName = Some(c2.firstName),
              lastName = Some(c2.lastName),
              middleName = Some(c2.middleName),
              email = Some(None),
              url = None,
              description = None,
              phone = Some(None),
              language = Some(c2.language),
              //tenants = None,
              //applications = None,
              //roles = None,
              password = Some(c2.password),
              avatarUrl = None,
              sphere = None,
              company = None,
              position = None,
              rank = None,
              additionalTel = None,
              additionalMail = None,
              meta = None,
              status = None)
          })

        for {
          cc1 <- f1
          cc2 <- ask(actor, UserManager.UpdateUserCmd(cc1))
        } yield cc2 shouldBe a[LoginRequiredMsg]
      }

      "should not update user if email already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val actor = newUserActor()

        for {
          cc1 <- ask(actor, UserManager.CreateUserCmd(c1))
          cc2 <- ask(actor, UserManager.CreateUserCmd(c2)).mapTo[CreateUserSuccess].map(_.x)
          cc3 <- ask(actor, UserManager.UpdateUserCmd(UpdateUser(
            id = cc2.id,
            username = None,
            displayName = None,
            firstName = Some(c2.firstName),
            lastName = Some(c2.lastName),
            middleName = Some(c2.middleName),
            email = Some(c1.email),
            url = None,
            description = None,
            phone = Some(None),
            language = Some(c2.language),
            //tenants = None,
            //applications = None,
            //roles = None,
            password = Some(c2.password),
            avatarUrl = None,
            sphere = None,
            company = None,
            position = None,
            rank = None,
            additionalTel = None,
            additionalMail = None,
            meta = None,
            status = None)))
        } yield cc3 shouldBe a[EmailAlreadyExistsMsg]
      }

    }

    "DeleteUserCmd" must {
      "delete user" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserManager.CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc2 <- ask(actor, UserManager.DeleteUserCmd(cc1.id))
          ccs <- ask(actor, UserManager.FindAllUsers).mapTo[UserManager.MultipleUsers].map(_.entries)
        } yield ccs.size shouldBe 0
      }

      "should not delete if user not exists" in {
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserManager.DeleteUserCmd(UUID.randomUUID()))
        } yield cc1 shouldBe a[UserNotFoundMsg]
      }
    }

    "FindUserByLoginAndPassword" must {
      "find user for correct password" in {
        val c1 = newCreateUser(email = Some("   valery@valery.com   "), phone = Some("   +712345   "), login = Some("   valery   ")).copy(password = "abc")
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserManager.CreateUserCmd(c1))
          cc2 <- ask(actor, UserManager.FindUserByLoginAndPassword(c1.email.get.toUpperCase.trim + " ", "abc")).mapTo[UserManager.SingleUser].map(_.maybeEntry.get)
          cc3 <- ask(actor, UserManager.FindUserByLoginAndPassword(c1.phone.get.toUpperCase.trim + " ", "abc")).mapTo[UserManager.SingleUser].map(_.maybeEntry.get)
          cc4 <- ask(actor, UserManager.FindUserByLoginAndPassword(c1.username.get.toUpperCase.trim + " ", "abc")).mapTo[UserManager.SingleUser].map(_.maybeEntry.get)
        } yield {
          cc2 shouldBe a[User]
          cc3 shouldBe a[User]
          cc4 shouldBe a[User]
        }
      }

    }
  }

}