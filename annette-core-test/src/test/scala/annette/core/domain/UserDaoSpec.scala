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
import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.application.ApplicationService
import annette.core.domain.application.model.{ Application, ApplicationUpdate }
import annette.core.domain.language.LanguageService
import annette.core.domain.language.model.{ Language, LanguageUpdate }
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.dao.UserDao
import annette.core.domain.tenancy.model._
import annette.core.test.PersistenceSpec

class UserDaoSpec extends TestKit(ActorSystem("UserActorSpec"))
  with PersistenceSpec
  with NewApplication
  with NewLanguage
  with NewUser {

  def newCoreServiceActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(CoreService.props, s"CoreService-$uuid")
  }

  def newUserDao(): UserDao = {
    val coreServiceActor = newCoreServiceActor()
    new UserDao(coreServiceActor)
  }

  "A UserDao" when call {
    "create" must {
      "create new user" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "abc")
          cc2 <- dao.create(c2, "abc")
          ccs <- dao.selectAll
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          ccs.head shouldBe c1
          ccs.last shouldBe c2
        }
      }

      "should not create new user if there are no email & phone & login" in {
        val c1 = newUser()
        val dao = newUserDao()
        for {
          cc1 <- recoverToExceptionIf[LoginRequired](dao.create(c1, ""))
        } yield {
          cc1.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginRequired")
        }
      }

      "should not create new user if it already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = c1.copy(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val dao = newUserDao()
        for {

          cc1 <- dao.create(c1, "")
          cc2 <- recoverToExceptionIf[UserAlreadyExists](dao.create(c2, ""))
        } yield {
          cc1 shouldBe ()
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.alreadyExists")
        }
      }
      "should not create new user if email already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = c1.email)
        val dao = newUserDao()
        for {

          cc1 <- dao.create(c1, "")
          cc2 <- recoverToExceptionIf[EmailAlreadyExists](dao.create(c2, ""))
        } yield {
          cc1 shouldBe ()
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.emailAlreadyExists")
        }
      }
      "should not create new user if phone already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(phone = c1.phone)
        val dao = newUserDao()
        for {

          cc1 <- dao.create(c1, "")
          cc2 <- recoverToExceptionIf[PhoneAlreadyExists](dao.create(c2, ""))
        } yield {
          cc1 shouldBe ()
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.phoneAlreadyExists")
        }
      }
      "should not create new user if login already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(login = c1.login)
        val dao = newUserDao()
        for {

          cc1 <- dao.create(c1, "")
          cc2 <- recoverToExceptionIf[LoginAlreadyExists](dao.create(c2, ""))
        } yield {
          cc1 shouldBe ()
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginAlreadyExists")
        }
      }

    }

    "update" must {
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
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "abc")
          cc2 <- dao.update(u1)
          ccs <- dao.getById(c1.id)
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          ccs shouldBe Some(c2)
        }
      }

      "update none data of user" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val u1 = UserUpdate(
          id = c1.id)
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "abc")
          cc2 <- dao.update(u1)
          ccs <- dao.getById(c1.id)
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          ccs shouldBe Some(c1)
        }
      }

      "should not update user if there are no email & phone & login" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val u1 = UserUpdate(
          email = Some(None),
          phone = Some(None),
          login = Some(None),
          id = c1.id)
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "")
          cc2 <- recoverToExceptionIf[LoginRequired](dao.update(u1))
        } yield {
          cc1 shouldBe ()
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginRequired")
        }
      }

      "should not update if user not exists" in {
        val u1 = UserUpdate(
          id = UUID.randomUUID())
        val dao = newUserDao()
        for {
          cc1 <- recoverToExceptionIf[UserNotFound](dao.update(u1))
        } yield {
          cc1.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.notFound")
        }
      }

      "should not update user if email already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val u2 = UserUpdate(
          email = Some(c1.email),
          id = c2.id)
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "")
          cc2 <- dao.create(c2, "")
          cc3 <- recoverToExceptionIf[EmailAlreadyExists](dao.update(u2))
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          cc3.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.emailAlreadyExists")
        }
      }
      "should not update user if phone already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val u2 = UserUpdate(
          phone = Some(c1.phone),
          id = c2.id)
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "")
          cc2 <- dao.create(c2, "")
          cc3 <- recoverToExceptionIf[PhoneAlreadyExists](dao.update(u2))
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          cc3.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.phoneAlreadyExists")
        }
      }

      "should not update user if login already exists" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val u2 = UserUpdate(
          login = Some(c1.login),
          id = c2.id)
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "")
          cc2 <- dao.create(c2, "")
          cc3 <- recoverToExceptionIf[LoginAlreadyExists](dao.update(u2))
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          cc3.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginAlreadyExists")
        }
      }

    }

    "delete" must {
      "delete user" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "abc")
          cc2 <- dao.delete(c1.id)
          ccs <- dao.selectAll
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe true
          ccs.size shouldBe 0
        }
      }

      "should not delete if user not exists" in {
        val dao = newUserDao()
        for {
          cc1 <- recoverToExceptionIf[UserNotFound](dao.delete(UUID.randomUUID()))
        } yield {
          cc1.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.notFound")
        }
      }
    }

    "findUserByLoginAndPassword" must {
      "find user for correct password" in {
        val c1 = newUser(email = Some("   valery@valery.com   "), phone = Some("   +712345   "), login = Some("   valery   "))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "abc")
          cc2 <- dao.getByLoginAndPassword(c1.email.get.toUpperCase.trim + " ", "abc")
          cc3 <- dao.getByLoginAndPassword(c1.phone.get.toUpperCase.trim + " ", "abc")
          cc4 <- dao.getByLoginAndPassword(c1.login.get.toUpperCase.trim + " ", "abc")
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe Some(c1)
          cc3 shouldBe Some(c1)
          cc4 shouldBe Some(c1)
        }
      }

      "don't find user for incorrect password" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "abc")
          cc2 <- dao.getByLoginAndPassword(c1.email.get, "abc1")
          cc3 <- dao.getByLoginAndPassword(c1.phone.get, "abc1")
          cc4 <- dao.getByLoginAndPassword(c1.login.get, "abc1")
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe None
          cc3 shouldBe None
          cc4 shouldBe None

        }
      }

      "don't find user for incorrect login" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "abc")
          cc2 <- dao.getByLoginAndPassword("", "abc1")
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe None
        }
      }
    }

    "setPassword" must {
      "update password" in {
        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1, "abc")
          cc5 <- dao.setPassword(c1.id, "abc1")
          cc2 <- dao.getByLoginAndPassword(c1.email.get, "abc1")
          cc3 <- dao.getByLoginAndPassword(c1.phone.get, "abc1")
          cc4 <- dao.getByLoginAndPassword(c1.login.get, "abc1")
        } yield {
          cc1 shouldBe ()
          cc5 shouldBe true
          cc2 shouldBe Some(c1)
          cc3 shouldBe Some(c1)
          cc4 shouldBe Some(c1)
        }
      }

      "don't update password for non existing user" in {
        val id = UUID.randomUUID()
        val dao = newUserDao()
        for {
          cc1 <- recoverToExceptionIf[UserNotFound](dao.setPassword(id, "abc1"))
        } yield {
          cc1.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.notFound")
        }
      }

    }
  }

}