package annette.core.domain

import java.util.UUID

import akka.Done
import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.application.ApplicationManager
import annette.core.domain.application._
import annette.core.domain.language.LanguageService
import annette.core.domain.language.model.{ Language, LanguageUpdate }
import annette.core.domain.tenancy.{ UserManager, _ }
import annette.core.domain.tenancy.model._
import annette.core.security.verification.VerificationBus
import annette.core.test.PersistenceSpec
import com.typesafe.config.{ Config, ConfigFactory }

class UserManagerSpec extends TestKit(ActorSystem("UserActorSpec"))
  with PersistenceSpec
  with NewApplication
  with NewLanguage
  with NewUser {
  lazy val config: Config = ConfigFactory.load()

  def newCoreServiceActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(CoreService.props(
      config = config,
      verificationBus = new VerificationBus), s"CoreService-$uuid")
  }

  def newUserDao(): UserManager = {
    val coreServiceActor = newCoreServiceActor()
    new UserManager(coreServiceActor)
  }

  "A UserDao" when {
    "create" must {
      "create new user" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- dao.create(c2)
          ccs <- dao.listUsers
        } yield {
          ccs.head.email shouldBe c1.email
          ccs.last.email shouldBe c2.email
        }
      }

      "should not create new user if there are no email & phone & login" in {
        val c1 = newCreateUser()
        val dao = newUserDao()
        for {
          cc1 <- recoverToExceptionIf[LoginRequired](dao.create(c1))
        } yield {
          cc1.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginRequired")
        }
      }

      //      "should not create new user if it already exists" in {
      //        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
      //        val c2 = c1.copy(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), username = Some("kantemirov1"))
      //        val dao = newUserDao()
      //        for {
      //
      //          cc1 <- dao.create(c1)
      //          cc2 <- recoverToExceptionIf[UserAlreadyExists](dao.create(c2))
      //        } yield {
      //          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.alreadyExists")
      //        }
      //      }
      "should not create new user if email already exists" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = c1.email)
        val dao = newUserDao()
        for {

          cc1 <- dao.create(c1)
          cc2 <- recoverToExceptionIf[EmailAlreadyExists](dao.create(c2))
        } yield {
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.emailAlreadyExists")
        }
      }
      "should not create new user if phone already exists" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(phone = c1.phone)
        val dao = newUserDao()
        for {

          cc1 <- dao.create(c1)
          cc2 <- recoverToExceptionIf[PhoneAlreadyExists](dao.create(c2))
        } yield {
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.phoneAlreadyExists")
        }
      }
      "should not create new user if login already exists" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(login = c1.username)
        val dao = newUserDao()
        for {

          cc1 <- dao.create(c1)
          cc2 <- recoverToExceptionIf[LoginAlreadyExists](dao.create(c2))
        } yield {
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginAlreadyExists")
        }
      }

    }

    //    "update" must {
    //      "update all data of user" in {
    //        val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
    //        val c2 = newUser(id = c1.id, email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
    //        val u1 = UserUpdate(
    //          lastName = Some(c2.lastName),
    //          firstName = Some(c2.firstName),
    //          middleName = Some(c2.middleName),
    //          email = Some(c2.email),
    //          phone = Some(c2.phone),
    //          login = Some(c2.username),
    //          defaultLanguage = Some(c2.defaultLanguage),
    //          id = c1.id)
    //        val dao = newUserDao()
    //        for {
    //          cc1 <- dao.create(c1, "abc")
    //          cc2 <- dao.update(u1)
    //          ccs <- dao.getById(c1.id)
    //        } yield {
    //          cc1 shouldBe ()
    //          cc2 shouldBe ()
    //          ccs shouldBe Some(c2)
    //        }
    //      }
    //
    //      "update none data of user" in {
    //        val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
    //        val u1 = UpdateUser(
    //          id = c1.id)
    //        val dao = newUserDao()
    //        for {
    //          cc1 <- dao.create(c1, "abc")
    //          cc2 <- dao.update(u1)
    //          ccs <- dao.getById(c1.id)
    //        } yield {
    //          cc1 shouldBe ()
    //          cc2 shouldBe ()
    //          ccs shouldBe Some(c1)
    //        }
    //      }
    //
    //      "should not update user if there are no email & phone & login" in {
    //        val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
    //        val u1 = UpdateUser(
    //          email = Some(None),
    //          phone = Some(None),
    //          login = Some(None),
    //          id = c1.id)
    //        val dao = newUserDao()
    //        for {
    //          cc1 <- dao.create(c1, "")
    //          cc2 <- recoverToExceptionIf[LoginRequired](dao.update(u1))
    //        } yield {
    //          cc1 shouldBe ()
    //          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginRequired")
    //        }
    //      }
    //
    //      "should not update if user not exists" in {
    //        val u1 = UserUpdate(
    //          id = UUID.randomUUID())
    //        val dao = newUserDao()
    //        for {
    //          cc1 <- recoverToExceptionIf[UserNotFound](dao.update(u1))
    //        } yield {
    //          cc1.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.notFound")
    //        }
    //      }
    //
    //      "should not update user if email already exists" in {
    //        val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
    //        val c2 = newUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
    //        val u2 = UserUpdate(
    //          email = Some(c1.email),
    //          id = c2.id)
    //        val dao = newUserDao()
    //        for {
    //          cc1 <- dao.create(c1, "")
    //          cc2 <- dao.create(c2, "")
    //          cc3 <- recoverToExceptionIf[EmailAlreadyExists](dao.update(u2))
    //        } yield {
    //          cc1 shouldBe ()
    //          cc2 shouldBe ()
    //          cc3.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.emailAlreadyExists")
    //        }
    //      }
    //      "should not update user if phone already exists" in {
    //        val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
    //        val c2 = newUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
    //        val u2 = UpdateUser(
    //          phone = Some(c1.phone),
    //          id = c2.id)
    //        val dao = newUserDao()
    //        for {
    //          cc1 <- dao.create(c1, "")
    //          cc2 <- dao.create(c2, "")
    //          cc3 <- recoverToExceptionIf[PhoneAlreadyExists](dao.update(u2))
    //        } yield {
    //          cc1 shouldBe ()
    //          cc2 shouldBe ()
    //          cc3.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.phoneAlreadyExists")
    //        }
    //      }
    //
    //      "should not update user if login already exists" in {
    //        val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
    //        val c2 = newUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
    //        val u2 = UserUpdate(
    //          login = Some(c1.username),
    //          id = c2.id)
    //        val dao = newUserDao()
    //        for {
    //          cc1 <- dao.create(c1).mapTo[CreateUserSuccess].map(_.x)
    //          cc2 <- dao.create(c2)
    //          cc3 <- recoverToExceptionIf[LoginAlreadyExists](dao.update(u2))
    //        } yield {
    //          cc1 shouldBe ()
    //          cc2 shouldBe ()
    //          cc3.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginAlreadyExists")
    //        }
    //      }
    //
    //    }

    "delete" must {
      "delete user" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- dao.delete(cc1.id)
          ccs <- dao.listUsers
        } yield ccs.size shouldBe 0
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
        val c1 = newCreateUser(email = Some("   kantemirov@kantemirov.com   "), phone = Some("   +712345   "), login = Some("   kantemirov   ")).copy(password = "abc")
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- dao.getByLoginAndPassword(c1.email.get.toUpperCase.trim + " ", "abc")
          cc3 <- dao.getByLoginAndPassword(c1.phone.get.toUpperCase.trim + " ", "abc")
          cc4 <- dao.getByLoginAndPassword(c1.username.get.toUpperCase.trim + " ", "abc")
        } yield {
          cc2 should not be empty
          cc3 should not be empty
          cc4 should not be empty
        }
      }

      "don't find user for incorrect password" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- dao.getByLoginAndPassword(c1.email.get, "abc1")
          cc3 <- dao.getByLoginAndPassword(c1.phone.get, "abc1")
          cc4 <- dao.getByLoginAndPassword(c1.username.get, "abc1")
        } yield {
          cc2 shouldBe empty
          cc3 shouldBe empty
          cc4 shouldBe empty

        }
      }

      "don't find user for incorrect login" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- dao.getByLoginAndPassword("", "abc1")
        } yield cc2 shouldBe empty
      }
    }

    "setPassword" must {
      "update password" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val dao = newUserDao()
        for {
          cc1 <- dao.create(c1)
          cc5 <- dao.setPassword(cc1.id, "abc1")
          cc2 <- dao.getByLoginAndPassword(c1.email.get, "abc1")
          cc3 <- dao.getByLoginAndPassword(c1.phone.get, "abc1")
          cc4 <- dao.getByLoginAndPassword(c1.username.get, "abc1")
        } yield {
          cc2 should not be empty
          cc3 should not be empty
          cc4 should not be empty
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