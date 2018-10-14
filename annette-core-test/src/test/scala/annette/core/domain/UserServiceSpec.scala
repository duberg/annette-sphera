package annette.core.domain

import java.util.UUID

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.TestKit
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.{ UserService, _ }
import annette.core.security.verification.VerificationBus
import annette.core.test.PersistenceSpec
import com.typesafe.config.{ Config, ConfigFactory }

class UserServiceSpec extends TestKit(ActorSystem("UserActorSpec"))
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

  def newUserDao(): UserService = {
    val coreServiceActor = newCoreServiceActor()
    new UserService(coreServiceActor)
  }

  "A UserDao" when {
    "createUser" must {
      "createUser new user" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val dao = newUserDao()
        for {
          cc1 <- dao.createUser(c1)
          cc2 <- dao.createUser(c2)
          ccs <- dao.listUsers
        } yield {
          ccs.head.email shouldBe c1.email
          ccs.last.email shouldBe c2.email
        }
      }

      "should not createUser new user if there are no email & phone & login" in {
        val c1 = newCreateUser()
        val dao = newUserDao()
        for {
          cc1 <- recoverToExceptionIf[LoginRequired](dao.createUser(c1))
        } yield {
          cc1.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginRequired")
        }
      }

      "should not createUser new user if it already exists" in {
        val userId = generateUUID
        val c1 = newCreateUser(id = Some(userId), email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = c1.copy(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), username = Some("kantemirov1"))
        val dao = newUserDao()
        for {

          x1 <- dao.createUser(c1)
          x2 <- recoverToExceptionIf[UserAlreadyExists](dao.createUser(c2))
        } yield {
          x2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.alreadyExists")
        }
      }
      "should not createUser new user if email already exists" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = c1.email)
        val dao = newUserDao()
        for {

          cc1 <- dao.createUser(c1)
          cc2 <- recoverToExceptionIf[EmailAlreadyExists](dao.createUser(c2))
        } yield {
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.emailAlreadyExists")
        }
      }
      "should not createUser new user if phone already exists" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(phone = c1.phone)
        val dao = newUserDao()
        for {

          cc1 <- dao.createUser(c1)
          cc2 <- recoverToExceptionIf[PhoneAlreadyExists](dao.createUser(c2))
        } yield {
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.phoneAlreadyExists")
        }
      }
      "should not createUser new user if login already exists" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(login = c1.username)
        val dao = newUserDao()
        for {

          cc1 <- dao.createUser(c1)
          cc2 <- recoverToExceptionIf[LoginAlreadyExists](dao.createUser(c2))
        } yield {
          cc2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginAlreadyExists")
        }
      }

    }

    "updateUser" must {
      "updateUser all data of user" in {
        val userId = generateUUID
        val c1 = newCreateUser(id = Some(userId), email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(id = Some(userId), email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val u1 = UpdateUser(
          id = userId,
          username = Some(c2.username),
          displayName = Some(c2.displayName),
          firstName = Some(c2.firstName),
          lastName = Some(c2.lastName),
          middleName = Some(c2.middleName),
          gender = Some(c2.gender),
          email = Some(c2.email),
          url = Some(c2.url),
          description = Some(c2.description),
          phone = Some(c2.phone),
          language = Some(c2.language),
          roles = c2.roles,
          password = Some(c2.password),
          avatarUrl = Some(c2.avatarUrl),
          sphere = Some(c2.sphere),
          company = Some(c2.company),
          position = Some(c2.position),
          rank = Some(c2.rank),
          additionalTel = Some(c2.additionalTel),
          additionalMail = Some(c2.additionalMail),
          meta = c2.meta,
          status = c2.status)

        val dao = newUserDao()
        for {
          x1 <- dao.createUser(c1)
          x2 <- dao.updateUser(u1)
          Some(x3) <- dao.getUserById(userId)
        } yield {
          x3.id shouldBe u1.id
          Some(x3.username) shouldBe u1.username
          Some(x3.displayName) shouldBe u1.displayName
          Some(x3.firstName) shouldBe u1.firstName
          Some(x3.lastName) shouldBe u1.lastName
          Some(x3.middleName) shouldBe u1.middleName
          Some(x3.gender) shouldBe u1.gender
          Some(x3.email) shouldBe u1.email
          Some(x3.url) shouldBe u1.url
          Some(x3.description) shouldBe u1.description
          Some(x3.phone) shouldBe u1.phone
          Some(x3.language) shouldBe u1.language
          Some(x3.roles) shouldBe Some(Map.empty)
          //Some(x3.password) shouldBe u1.password
          Some(x3.avatarUrl) shouldBe u1.avatarUrl
          Some(x3.sphere) shouldBe u1.sphere
          Some(x3.company) shouldBe u1.company
          Some(x3.position) shouldBe u1.position
          Some(x3.rank) shouldBe u1.rank
          Some(x3.additionalTel) shouldBe u1.additionalTel
          Some(x3.additionalMail) shouldBe u1.additionalMail
          Some(x3.meta) shouldBe Some(Map.empty)
          Some(x3.status) shouldBe u1.status
        }
      }

      "fail if email, password, phone == Some(None), Some(None), Some(None)" in {
        val userId = generateUUID
        val c1 = newCreateUser(id = Some(userId), email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(id = Some(userId), email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val u1 = UpdateUser(
          id = userId,
          username = Some(None),
          displayName = Some(c2.displayName),
          firstName = Some(c2.firstName),
          lastName = Some(c2.lastName),
          middleName = Some(c2.middleName),
          gender = Some(c2.gender),
          email = Some(None),
          url = Some(c2.url),
          description = Some(c2.description),
          phone = Some(None),
          language = Some(c2.language),
          roles = c2.roles,
          password = Some(c2.password),
          avatarUrl = Some(c2.avatarUrl),
          sphere = Some(c2.sphere),
          company = Some(c2.company),
          position = Some(c2.position),
          rank = Some(c2.rank),
          additionalTel = Some(c2.additionalTel),
          additionalMail = Some(c2.additionalMail),
          meta = c2.meta,
          status = c2.status)

        val dao = newUserDao()
        for {
          x1 <- dao.createUser(c1)
          x2 <- recoverToExceptionIf[LoginRequired](dao.updateUser(u1))
        } yield x2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginRequired")
      }

      "email, password, phone == None, None, None" in {
        val userId = generateUUID
        val c1 = newCreateUser(id = Some(userId), email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(id = Some(userId), email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val u1 = UpdateUser(
          id = userId,
          username = None,
          displayName = Some(c2.displayName),
          firstName = Some(c2.firstName),
          lastName = Some(c2.lastName),
          middleName = Some(c2.middleName),
          gender = Some(c2.gender),
          email = None,
          url = Some(c2.url),
          description = Some(c2.description),
          phone = None,
          language = Some(c2.language),
          roles = c2.roles,
          password = Some(c2.password),
          avatarUrl = Some(c2.avatarUrl),
          sphere = Some(c2.sphere),
          company = Some(c2.company),
          position = Some(c2.position),
          rank = Some(c2.rank),
          additionalTel = Some(c2.additionalTel),
          additionalMail = Some(c2.additionalMail),
          meta = c2.meta,
          status = c2.status)

        val dao = newUserDao()
        for {
          x1 <- dao.createUser(c1)
          x2 <- dao.updateUser(u1)
        } yield succeed
      }
      //
      //          "updateUser none data of user" in {
      //            val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
      //            val u1 = UpdateUser(
      //              id = c1.id)
      //            val dao = newUserDao()
      //            for {
      //              x1 <- dao.createUser(c1, "abc")
      //              x2 <- dao.updateUser(u1)
      //              x3 <- dao.getById(c1.id)
      //            } yield {
      //              x1 shouldBe ()
      //              x2 shouldBe ()
      //              x3 shouldBe Some(c1)
      //            }
      //          }
      //
      //          "should not updateUser user if there are no email & phone & login" in {
      //            val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
      //            val u1 = UpdateUser(
      //              email = Some(None),
      //              phone = Some(None),
      //              login = Some(None),
      //              id = c1.id)
      //            val dao = newUserDao()
      //            for {
      //              x1 <- dao.createUser(c1, "")
      //              x2 <- recoverToExceptionIf[LoginRequired](dao.updateUser(u1))
      //            } yield {
      //              x1 shouldBe ()
      //              x2.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginRequired")
      //            }
      //          }
      //
      //          "should not updateUser if user not exists" in {
      //            val u1 = UserUpdate(
      //              id = UUID.randomUUID())
      //            val dao = newUserDao()
      //            for {
      //              x1 <- recoverToExceptionIf[UserNotFound](dao.updateUser(u1))
      //            } yield {
      //              x1.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.notFound")
      //            }
      //          }
      //
      //          "should not updateUser user if email already exists" in {
      //            val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
      //            val c2 = newUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
      //            val u2 = UserUpdate(
      //              email = Some(c1.email),
      //              id = c2.id)
      //            val dao = newUserDao()
      //            for {
      //              x1 <- dao.createUser(c1, "")
      //              x2 <- dao.createUser(c2, "")
      //              cc3 <- recoverToExceptionIf[EmailAlreadyExists](dao.updateUser(u2))
      //            } yield {
      //              x1 shouldBe ()
      //              x2 shouldBe ()
      //              cc3.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.emailAlreadyExists")
      //            }
      //          }
      //          "should not updateUser user if phone already exists" in {
      //            val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
      //            val c2 = newUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
      //            val u2 = UpdateUser(
      //              phone = Some(c1.phone),
      //              id = c2.id)
      //            val dao = newUserDao()
      //            for {
      //              x1 <- dao.createUser(c1, "")
      //              x2 <- dao.createUser(c2, "")
      //              cc3 <- recoverToExceptionIf[PhoneAlreadyExists](dao.updateUser(u2))
      //            } yield {
      //              x1 shouldBe ()
      //              x2 shouldBe ()
      //              cc3.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.phoneAlreadyExists")
      //            }
      //          }
      //
      //          "should not updateUser user if login already exists" in {
      //            val c1 = newUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
      //            val c2 = newUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
      //            val u2 = UserUpdate(
      //              login = Some(c1.username),
      //              id = c2.id)
      //            val dao = newUserDao()
      //            for {
      //              x1 <- dao.createUser(c1).mapTo[CreateUserSuccess].map(_.x)
      //              x2 <- dao.createUser(c2)
      //              cc3 <- recoverToExceptionIf[LoginAlreadyExists](dao.updateUser(u2))
      //            } yield {
      //              x1 shouldBe ()
      //              x2 shouldBe ()
      //              cc3.exceptionMessage.get("code") shouldBe Some("core.tenancy.user.loginAlreadyExists")
      //            }
      //          }

    }

    "delete" must {
      "deleteUser user" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val dao = newUserDao()
        for {
          cc1 <- dao.createUser(c1)
          cc2 <- dao.deleteUser(cc1.id)
          ccs <- dao.listUsers
        } yield ccs.size shouldBe 0
      }

      "should not deleteUser if user not exists" in {
        val dao = newUserDao()
        for {
          cc1 <- recoverToExceptionIf[UserNotFound](dao.deleteUser(UUID.randomUUID()))
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
          cc1 <- dao.createUser(c1)
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
          cc1 <- dao.createUser(c1)
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
          cc1 <- dao.createUser(c1)
          cc2 <- dao.getByLoginAndPassword("", "abc1")
        } yield cc2 shouldBe empty
      }
    }

    "setPassword" must {
      "updateUser password" in {
        val c1 = newCreateUser(email = Some("kantemirov@kantemirov.com"), phone = Some("+712345"), login = Some("kantemirov"))
        val dao = newUserDao()
        for {
          cc1 <- dao.createUser(c1)
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

      "don't updateUser password for non existing user" in {
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