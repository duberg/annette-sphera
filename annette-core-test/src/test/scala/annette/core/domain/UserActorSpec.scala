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
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model._
import annette.core.test.PersistenceSpec

class UserActorSpec extends TestKit(ActorSystem("UserActorSpec"))
  with PersistenceSpec with NewUser {

  "A UserActor" when receive {
    "CreateUserCmd" must {
      "create new user" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, "abc"))
          cc2 <- ask(actor, UserService.CreateUserCmd(c2, "abc"))
          ccs <- ask(actor, UserService.FindAllUsers).mapTo[UserService.MultipleUsers].map(_.entries)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs(c1.id) shouldBe c1
          ccs(c2.id) shouldBe c2
        }
      }

      "should not create new user if there are no email & phone & login" in {
        val c1 = newUser()
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, ""))
        } yield {
          cc1 shouldBe a[LoginRequiredMsg]
        }
      }

      "should not create new user if it already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = c1.copy(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val actor = newUserActor()
        for {

          cc1 <- ask(actor, UserService.CreateUserCmd(c1, ""))
          cc2 <- ask(actor, UserService.CreateUserCmd(c2, ""))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe a[UserAlreadyExistsMsg]
        }
      }
      "should not create new user if email already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = c1.email)
        val actor = newUserActor()
        for {

          cc1 <- ask(actor, UserService.CreateUserCmd(c1, ""))
          cc2 <- ask(actor, UserService.CreateUserCmd(c2, ""))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe a[EmailAlreadyExistsMsg]
        }
      }
      "should not create new user if phone already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(phone = c1.phone)
        val actor = newUserActor()
        for {

          cc1 <- ask(actor, UserService.CreateUserCmd(c1, ""))
          cc2 <- ask(actor, UserService.CreateUserCmd(c2, ""))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe a[PhoneAlreadyExistsMsg]
        }
      }
      "should not create new user if login already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(login = c1.login)
        val actor = newUserActor()
        for {

          cc1 <- ask(actor, UserService.CreateUserCmd(c1, ""))
          cc2 <- ask(actor, UserService.CreateUserCmd(c2, ""))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe a[LoginAlreadyExistsMsg]
        }
      }

    }

    "UpdateUserCmd" must {
      "update all data of user" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(id = c1.id, email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val u1 = UserUpdate(
          lastname = Some(c2.lastname),
          firstname = Some(c2.firstname),
          middlename = Some(c2.middlename),
          email = Some(c2.email),
          phone = Some(c2.phone),
          login = Some(c2.login),
          defaultLanguage = Some(c2.defaultLanguage),
          id = c1.id)
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, "abc"))
          cc2 <- ask(actor, UserService.UpdateUserCmd(u1))
          ccs <- ask(actor, UserService.FindUserById(c1.id)).mapTo[UserService.SingleUser].map(_.maybeEntry.get)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs shouldBe c2
        }
      }

      "update none data of user" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val u1 = UserUpdate(
          id = c1.id)
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, "abc"))
          cc2 <- ask(actor, UserService.UpdateUserCmd(u1))
          ccs <- ask(actor, UserService.FindUserById(c1.id)).mapTo[UserService.SingleUser].map(_.maybeEntry.get)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs shouldBe c1
        }
      }

      "should not update user if there are no email & phone & login" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val u1 = UserUpdate(
          email = Some(None),
          phone = Some(None),
          login = Some(None),
          id = c1.id)
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, ""))
          cc2 <- ask(actor, UserService.UpdateUserCmd(u1))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe a[LoginRequiredMsg]
        }
      }

      "should not update if user not exists" in {
        val u1 = UserUpdate(
          id = UUID.randomUUID())
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.UpdateUserCmd(u1))
        } yield {
          cc1 shouldBe a[UserNotFoundMsg]
        }
      }

      "should not update user if email already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val u2 = UserUpdate(
          email = Some(c1.email),
          id = c2.id)
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, ""))
          cc2 <- ask(actor, UserService.CreateUserCmd(c2, ""))
          cc3 <- ask(actor, UserService.UpdateUserCmd(u2))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          cc3 shouldBe a[EmailAlreadyExistsMsg]
        }
      }
      "should not update user if phone already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val u2 = UserUpdate(
          phone = Some(c1.phone),
          id = c2.id)
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, ""))
          cc2 <- ask(actor, UserService.CreateUserCmd(c2, ""))
          cc3 <- ask(actor, UserService.UpdateUserCmd(u2))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          cc3 shouldBe a[PhoneAlreadyExistsMsg]
        }
      }

      "should not update user if login already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val u2 = UserUpdate(
          login = Some(c1.login),
          id = c2.id)
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, ""))
          cc2 <- ask(actor, UserService.CreateUserCmd(c2, ""))
          cc3 <- ask(actor, UserService.UpdateUserCmd(u2))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          cc3 shouldBe a[LoginAlreadyExistsMsg]
        }
      }

    }

    "DeleteUserCmd" must {
      "delete user" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, "abc"))
          cc2 <- ask(actor, UserService.DeleteUserCmd(c1.id))
          ccs <- ask(actor, UserService.FindAllUsers).mapTo[UserService.MultipleUsers].map(_.entries)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs.size shouldBe 0
        }
      }

      "should not delete if user not exists" in {
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.DeleteUserCmd(UUID.randomUUID()))
        } yield {
          cc1 shouldBe a[UserNotFoundMsg]
        }
      }
    }

    "FindUserByLoginAndPassword" must {
      "find user for correct password" in {
        val c1 = newUser(email = Some("   valery@valery.com   "), phone = Some("   +712345   "), login = Some("   valery   "))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, "abc"))
          cc2 <- ask(actor, UserService.FindUserByLoginAndPassword(c1.email.get.toUpperCase.trim + " ", "abc")).mapTo[UserService.SingleUser].map(_.maybeEntry.get)
          cc3 <- ask(actor, UserService.FindUserByLoginAndPassword(c1.phone.get.toUpperCase.trim + " ", "abc")).mapTo[UserService.SingleUser].map(_.maybeEntry.get)
          cc4 <- ask(actor, UserService.FindUserByLoginAndPassword(c1.login.get.toUpperCase.trim + " ", "abc")).mapTo[UserService.SingleUser].map(_.maybeEntry.get)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe c1
          cc3 shouldBe c1
          cc4 shouldBe c1

        }
      }

      "don't find user for incorrect password" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, "abc"))
          cc2 <- ask(actor, UserService.FindUserByLoginAndPassword(c1.email.get, "abc1")).mapTo[UserService.SingleUser].map(_.maybeEntry)
          cc3 <- ask(actor, UserService.FindUserByLoginAndPassword(c1.phone.get, "abc1")).mapTo[UserService.SingleUser].map(_.maybeEntry)
          cc4 <- ask(actor, UserService.FindUserByLoginAndPassword(c1.login.get, "abc1")).mapTo[UserService.SingleUser].map(_.maybeEntry)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe None
          cc3 shouldBe None
          cc4 shouldBe None

        }
      }

      "don't find user for incorrect login" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, "abc"))
          cc2 <- ask(actor, UserService.FindUserByLoginAndPassword("", "abc1")).mapTo[UserService.SingleUser].map(_.maybeEntry)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe None
        }
      }
    }

    "UpdatePasswordCmd" must {
      "update password" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.CreateUserCmd(c1, "abc"))
          cc5 <- ask(actor, UserService.UpdatePasswordCmd(c1.id, "abc1"))
          cc2 <- ask(actor, UserService.FindUserByLoginAndPassword(c1.email.get, "abc1")).mapTo[UserService.SingleUser].map(_.maybeEntry.get)
          cc3 <- ask(actor, UserService.FindUserByLoginAndPassword(c1.phone.get, "abc1")).mapTo[UserService.SingleUser].map(_.maybeEntry.get)
          cc4 <- ask(actor, UserService.FindUserByLoginAndPassword(c1.login.get, "abc1")).mapTo[UserService.SingleUser].map(_.maybeEntry.get)
        } yield {
          cc1 shouldBe Done
          cc5 shouldBe Done
          cc2 shouldBe c1
          cc3 shouldBe c1
          cc4 shouldBe c1
        }
      }

      "don't update password for non existing user" in {
        val id = UUID.randomUUID()
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, UserService.UpdatePasswordCmd(id, "abc1"))
        } yield {
          cc1 shouldBe UserNotFoundMsg(id)
        }
      }

    }
  }

}