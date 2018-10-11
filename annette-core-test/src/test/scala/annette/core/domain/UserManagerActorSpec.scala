package annette.core.domain

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model.User._
import annette.core.domain.tenancy.model._
import annette.core.test.PersistenceSpec

class UserManagerActorSpec extends TestKit(ActorSystem("UserActorSpec"))
  with PersistenceSpec with NewUser {

  "A UserActor" when receive {
    "CreateUserCmd" must {
      "create new user" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov1.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc2 <- ask(actor, CreateUserCmd(c2)).mapTo[CreateUserSuccess].map(_.x)
          ccs <- ask(actor, ListUsers).mapTo[UsersMap].map(_.x)
        } yield {
          ccs(cc1.id) shouldBe a[User]
          ccs(cc2.id) shouldBe a[User]
        }
      }

      "should not create new user if there are no email & phone & login" in {
        val c1 = newCreateUser()
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, CreateUserCmd(c1))
        } yield {
          cc1 shouldBe a[LoginRequiredMsg]
        }
      }

      //      "should not create new user if it already exists" in {
      //        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
      //        val c2 = c1.copy(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), username = Some("kantemirov1"))
      //        val actor = newUserActor()
      //        for {
      //
      //          cc1 <- ask(actor, UserService.CreateUserCmd(c1))
      //          cc2 <- ask(actor, UserService.CreateUserCmd(c2))
      //        } yield cc2 shouldBe a[UserAlreadyExistsMsg]
      //      }
      "should not create new user if email already exists" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = c1.email)
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, CreateUserCmd(c1))
          cc2 <- ask(actor, CreateUserCmd(c2))
        } yield cc2 shouldBe a[EmailAlreadyExistsMsg]
      }
      "should not create new user if phone already exists" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(phone = c1.phone)
        val actor = newUserActor()
        for {

          cc1 <- ask(actor, CreateUserCmd(c1))
          cc2 <- ask(actor, CreateUserCmd(c2))
        } yield cc2 shouldBe a[PhoneAlreadyExistsMsg]
      }
      "should not create new user if login already exists" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(login = c1.username)
        val actor = newUserActor()
        for {

          cc1 <- ask(actor, CreateUserCmd(c1))
          cc2 <- ask(actor, CreateUserCmd(c2))
        } yield cc2 shouldBe a[LoginAlreadyExistsMsg]
      }

    }

    "UpdateUserCmd" must {
      "update all data of user" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))

        val actor = newUserActor()

        for {
          cc1 <- ask(actor, CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc2 <- ask(actor, UpdateUserCmd(
            UpdateUser(
              id = cc1.id,
              username = Some(c2.username),
              displayName = None,
              firstName = Some(c2.firstName),
              lastName = Some(c2.lastName),
              middleName = Some(c2.middleName),
              gender = Some(c2.gender),
              email = None,
              url = None,
              description = None,
              phone = None,
              language = Some(c2.language),
              roles = Some(c2.roles),
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
          ccs <- ask(actor, GetUserById(cc1.id)).mapTo[UserOpt].map(_.maybeEntry.get)
        } yield {
          ccs shouldBe a[User]
        }
      }

      "should not update user if there are no email & phone & login" in {
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))

        val actor = newUserActor()

        val f1 = ask(actor, CreateUserCmd(c2))
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
              gender = Some(c2.gender),
              email = Some(None),
              url = None,
              description = None,
              phone = Some(None),
              language = Some(c2.language),
              roles = Some(c2.roles),
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
          cc2 <- ask(actor, UpdateUserCmd(cc1))
        } yield cc2 shouldBe a[LoginRequiredMsg]
      }

      "should not update user if email already exists" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov1.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val actor = newUserActor()

        for {
          cc1 <- ask(actor, CreateUserCmd(c1))
          cc2 <- ask(actor, CreateUserCmd(c2)).mapTo[CreateUserSuccess].map(_.x)
          cc3 <- ask(actor, UpdateUserCmd(UpdateUser(
            id = cc2.id,
            username = None,
            displayName = None,
            firstName = Some(c2.firstName),
            lastName = Some(c2.lastName),
            middleName = Some(c2.middleName),
            gender = Some(c2.gender),
            email = Some(c1.email),
            url = None,
            description = None,
            phone = Some(None),
            language = Some(c2.language),
            roles = Some(c2.roles),
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
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc2 <- ask(actor, DeleteUserCmd(cc1.id))
          ccs <- ask(actor, ListUsers).mapTo[UsersMap].map(_.x)
        } yield ccs.size shouldBe 0
      }

      "should not delete if user not exists" in {
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, DeleteUserCmd(UUID.randomUUID()))
        } yield cc1 shouldBe a[UserNotFoundMsg]
      }
    }

    "FindUserByLoginAndPassword" must {
      "find user for correct password" in {
        val c1 = newCreateUser(email = Some("   it@kantemirov.ru   "), phone = Some("   +712345   "), login = Some("   kantemirov   ")).copy(password = "abc")
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, CreateUserCmd(c1))
          cc2 <- ask(actor, GetUserByLoginAndPassword(c1.email.get.toUpperCase.trim + " ", "abc")).mapTo[UserOpt].map(_.maybeEntry.get)
          cc3 <- ask(actor, GetUserByLoginAndPassword(c1.phone.get.toUpperCase.trim + " ", "abc")).mapTo[UserOpt].map(_.maybeEntry.get)
          cc4 <- ask(actor, GetUserByLoginAndPassword(c1.username.get.toUpperCase.trim + " ", "abc")).mapTo[UserOpt].map(_.maybeEntry.get)
        } yield {
          cc2 shouldBe a[User]
          cc3 shouldBe a[User]
          cc4 shouldBe a[User]
        }
      }

    }
  }

}