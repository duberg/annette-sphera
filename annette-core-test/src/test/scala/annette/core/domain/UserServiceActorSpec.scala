package annette.core.domain

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model.User._
import annette.core.domain.tenancy.model._
import annette.core.test.PersistenceSpec

class UserServiceActorSpec extends TestKit(ActorSystem("UserActorSpec"))
  with PersistenceSpec with NewUser {

  "A UserActor" when receive {
    "CreateUserCmd" must {
      "createUser new user" in {
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

      "should not createUser new user if there are no email & phone & login" in {
        val c1 = newCreateUser()
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, CreateUserCmd(c1))
        } yield {
          cc1 shouldBe a[LoginRequiredMsg]
        }
      }

      //      "should not createUser new user if it already exists" in {
      //        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
      //        val c2 = c1.copy(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), username = Some("kantemirov1"))
      //        val actor = newUserActor()
      //        for {
      //
      //          x1 <- ask(actor, UserService.CreateUserCmd(c1))
      //          x2 <- ask(actor, UserService.CreateUserCmd(c2))
      //        } yield x2 shouldBe a[UserAlreadyExistsMsg]
      //      }
      "should not createUser new user if email already exists" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = c1.email)
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, CreateUserCmd(c1))
          cc2 <- ask(actor, CreateUserCmd(c2))
        } yield cc2 shouldBe a[EmailAlreadyExistsMsg]
      }
      "should not createUser new user if phone already exists" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(phone = c1.phone)
        val actor = newUserActor()
        for {

          cc1 <- ask(actor, CreateUserCmd(c1))
          cc2 <- ask(actor, CreateUserCmd(c2))
        } yield cc2 shouldBe a[PhoneAlreadyExistsMsg]
      }
      "should not createUser new user if login already exists" in {
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
      "updateUser all data of user" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))

        val actor = newUserActor()

        for {
          cc1 <- ask(actor, CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc2 <- ask(actor, UpdateUserCmd(
            UpdateUser(
              id = cc1.id,
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
              status = c2.status)))
          ccs <- ask(actor, GetUserById(cc1.id)).mapTo[UserOpt].map(_.maybeEntry.get)
        } yield {
          ccs shouldBe a[User]
        }
      }

      "should not updateUser user if there are no email & phone & login" in {
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))

        val actor = newUserActor()

        val f1 = ask(actor, CreateUserCmd(c2))
          .mapTo[CreateUserSuccess]
          .map(_.x)
          .map(user => {
            UpdateUser(
              id = user.id,
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
          })

        for {
          cc1 <- f1
          cc2 <- ask(actor, UpdateUserCmd(cc1))
        } yield cc2 shouldBe a[LoginRequiredMsg]
      }

      "should not updateUser user if email already exists" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov1.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val actor = newUserActor()

        for {
          cc1 <- ask(actor, CreateUserCmd(c1))
          cc2 <- ask(actor, CreateUserCmd(c2)).mapTo[CreateUserSuccess].map(_.x)
          cc3 <- ask(actor, UpdateUserCmd(UpdateUser(
            id = cc2.id,
            username = Some(None),
            displayName = Some(c2.displayName),
            firstName = Some(c2.firstName),
            lastName = Some(c2.lastName),
            middleName = Some(c2.middleName),
            gender = Some(c2.gender),
            email = Some(c1.email),
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
            status = c2.status)))
        } yield cc3 shouldBe a[EmailAlreadyExistsMsg]
      }

    }

    "DeleteUserCmd" must {
      "deleteUser user" in {
        val c1 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+712345"), login = Some("kantemirov"))
        val c2 = newCreateUser(email = Some("it@kantemirov.ru"), phone = Some("+7123451"), login = Some("kantemirov1"))
        val actor = newUserActor()
        for {
          cc1 <- ask(actor, CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc2 <- ask(actor, DeleteUserCmd(cc1.id))
          ccs <- ask(actor, ListUsers).mapTo[UsersMap].map(_.x)
        } yield ccs.size shouldBe 0
      }

      "should not deleteUser if user not exists" in {
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