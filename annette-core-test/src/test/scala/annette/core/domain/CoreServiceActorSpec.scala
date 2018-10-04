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
import annette.core.domain.tenancy._
import LastSessionService.LastSessionOpt
import OpenSessionService.{ OpenSessionOpt, OpenSessionSeq }
import annette.core.domain.tenancy.model.User.CreateUserSuccess
import annette.core.domain.tenancy.model._
import annette.core.security.verification.VerificationBus
import annette.core.test.PersistenceSpec
import com.typesafe.config.{ Config, ConfigFactory }
import org.joda.time.DateTime

class CoreServiceActorSpec extends TestKit(ActorSystem("CoreServiceActorSpec"))
  with PersistenceSpec
  with NewApplication
  with NewLanguage
  with NewUser
  with NewOpenSession
  with NewLastSession
  with NewSessionHistory {
  lazy val config: Config = ConfigFactory.load()

  def newCoreServiceActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(CoreService.props(config, new VerificationBus), s"CoreService-$uuid")
  }

  "A CoreServiceActor with ApplicationActor" when receive {
    "CreateApplicationCmd" must {
      "create new application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val a = newCoreServiceActor()
        for {
          cc1 <- ask(a, Application.CreateApplicationCmd(c1))
          cc2 <- ask(a, Application.CreateApplicationCmd(c2))
          ccs <- ask(a, Application.ListApplications).mapTo[Application.ApplicationsMap].map(_.x)
        } yield {
          ccs(c1.id) shouldBe c1
          ccs(c2.id) shouldBe c2
        }
      }
      "should not create new application if it already exists" in {
        val c1 = Application("App1", "app1", "APP1")
        val a = newCoreServiceActor()
        for {

          cc1 <- ask(a, Application.CreateApplicationCmd(c1))
          cc2 <- ask(a, Application.CreateApplicationCmd(c1))
        } yield cc2 shouldBe Application.EntryAlreadyExists
      }
    }

    "UpdateApplicationCmd" must {
      "update application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP1")
        val a = newCoreServiceActor()
        for {

          cc1 <- ask(a, Application.CreateApplicationCmd(c1))
          cc2 <- ask(a, Application.UpdateApplicationCmd(UpdateApplication(Some(c2.name), Some(c2.code), c1.id)))
          ccs <- ask(a, Application.GetApplicationById(c1.id)).mapTo[Application.ApplicationOpt].map(_.x)
        } yield ccs shouldBe Some(c2)
      }
      "should not update application if it doesn't exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val a = newCoreServiceActor()
        for {
          cc1 <- ask(a, Application.UpdateApplicationCmd(UpdateApplication(Some(c2.name), Some(c2.code), c1.id)))
        } yield cc1 shouldBe Application.EntryNotFound
      }
    }

    "DeleteApplicationCmd" must {
      "delete application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val a = newCoreServiceActor()
        for {
          cc1 <- ask(a, Application.CreateApplicationCmd(c1))
          cc2 <- ask(a, Application.CreateApplicationCmd(c2))
          ccs <- ask(a, Application.ListApplications).mapTo[Application.ApplicationsMap].map(_.x)
          d1 <- ask(a, Application.DeleteApplicationCmd(c1.id))
          ccr <- ask(a, Application.ListApplications).mapTo[Application.ApplicationsMap].map(_.x)
        } yield {
          // cc1 shouldBe Done
          //cc2 shouldBe Done
          ccs(c1.id) shouldBe c1
          ccs(c2.id) shouldBe c2
          //d1 shouldBe Done
          ccr(c2.id) shouldBe c2
          ccr.size shouldBe 1
        }
      }
      "should not delete application if it does not exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val a = newCoreServiceActor()
        for {
          d1 <- ask(a, Application.DeleteApplicationCmd(c1.id))
        } yield {
          d1 shouldBe Application.EntryNotFound
        }
      }
    }

  }

  "A CoreServiceActor with LanguageActor" when receive {
    "CreateLanguageCmd" must {
      "create new language" in {
        val c1 = Language("English", "EN")
        val c2 = Language("Russian", "RU")
        val a = newCoreServiceActor()
        for {
          cc1 <- ask(a, LanguageService.CreateLanguageCmd(c1))
          cc2 <- ask(a, LanguageService.CreateLanguageCmd(c2))
          ccs <- ask(a, LanguageService.FindAllLanguages).mapTo[LanguageService.MultipleLanguages].map(_.entries)
        } yield {
          ccs(c1.id) shouldBe c1
          ccs(c2.id) shouldBe c2
        }
      }
      "should not create new language if it already exists" in {
        val c1 = Language("English", "EN")
        val a = newCoreServiceActor()
        for {

          cc1 <- ask(a, LanguageService.CreateLanguageCmd(c1))
          cc2 <- ask(a, LanguageService.CreateLanguageCmd(c1))
        } yield {
          cc2 shouldBe LanguageService.EntryAlreadyExists
        }
      }
    }

    "UpdateLanguageCmd" must {
      "update language" in {
        val c1 = Language("English", "EN")
        val c2 = Language("English-US", "EN")
        val a = newCoreServiceActor()
        for {

          cc1 <- ask(a, LanguageService.CreateLanguageCmd(c1))
          cc2 <- ask(a, LanguageService.UpdateLanguageCmd(LanguageUpdate(Some(c2.name), c1.id)))
          ccs <- ask(a, LanguageService.FindLanguageById(c1.id)).mapTo[LanguageService.SingleLanguage].map(_.maybeEntry)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs shouldBe Some(c2)
        }
      }
      "should not update language if it doesn't exist" in {
        val c1 = Language("English", "EN")
        val c2 = Language("English-US", "EN")
        val a = newCoreServiceActor()
        for {
          cc1 <- ask(a, LanguageService.UpdateLanguageCmd(LanguageUpdate(Some(c2.name), c1.id)))
        } yield {
          cc1 shouldBe LanguageService.EntryNotFound
        }
      }
    }

    "DeleteLanguageCmd" must {
      "delete language" in {
        val c1 = Language("English", "EN")
        val c2 = Language("Russian", "RU")
        val a = newCoreServiceActor()
        for {
          cc1 <- ask(a, LanguageService.CreateLanguageCmd(c1))
          cc2 <- ask(a, LanguageService.CreateLanguageCmd(c2))
          ccs <- ask(a, LanguageService.FindAllLanguages).mapTo[LanguageService.MultipleLanguages].map(_.entries)
          d1 <- ask(a, LanguageService.DeleteLanguageCmd(c1.id))
          ccr <- ask(a, LanguageService.FindAllLanguages).mapTo[LanguageService.MultipleLanguages].map(_.entries)
        } yield {
          //cc1 shouldBe Done
          //cc2 shouldBe Done
          ccs(c1.id) shouldBe c1
          ccs(c2.id) shouldBe c2
          //d1 shouldBe Done
          ccr(c2.id) shouldBe c2
          ccr.size shouldBe 1
        }
      }
      "should not delete language if it does not exist" in {
        val c1 = Language("English", "EN")
        val a = newCoreServiceActor()
        for {
          d1 <- ask(a, LanguageService.DeleteLanguageCmd(c1.id))
        } yield {
          d1 shouldBe LanguageService.EntryNotFound
        }
      }
    }

  }

  "A CoreServiceActor with UserActor" when receive {
    "CreateUserCmd" must {
      "create new user" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1))
          cc2 <- ask(actor, User.CreateUserCmd(c2))
          ccs <- ask(actor, User.ListUsers).mapTo[User.UsersMap].map(_.x)
        } yield {
          cc1 shouldBe a[CreateUserSuccess]
          cc2 shouldBe a[CreateUserSuccess]
        }
      }

      "should not create new user if there are no email & phone & login" in {
        val c1 = newCreateUser()
        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1))
        } yield {
          cc1 shouldBe a[LoginRequiredMsg]
        }
      }

      //      "should not create new user if it already exists" in {
      //        val c1 = newUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
      //        val c2 = c1.copy(email = Some("valery1@valery.com"), phone = Some("+7123451"), username = Some("valery1"))
      //        val actor = newCoreServiceActor()
      //        for {
      //
      //          cc1 <- ask(actor, UserService.CreateUserCmd(c1))
      //          cc2 <- ask(actor, UserService.CreateUserCmd(c2))
      //        } yield {
      //          cc1 shouldBe a[CreateUserSuccess]
      //          cc2 shouldBe a[UserAlreadyExistsMsg]
      //        }
      //      }
      "should not create new user if email already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = c1.email)
        val actor = newCoreServiceActor()
        for {

          cc1 <- ask(actor, User.CreateUserCmd(c1))
          cc2 <- ask(actor, User.CreateUserCmd(c2))
        } yield {
          cc1 shouldBe a[CreateUserSuccess]
          cc2 shouldBe a[EmailAlreadyExistsMsg]
        }
      }
      "should not create new user if phone already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(phone = c1.phone)
        val actor = newCoreServiceActor()
        for {

          cc1 <- ask(actor, User.CreateUserCmd(c1))
          cc2 <- ask(actor, User.CreateUserCmd(c2))
        } yield {
          cc1 shouldBe a[CreateUserSuccess]
          cc2 shouldBe a[PhoneAlreadyExistsMsg]
        }
      }
      "should not create new user if login already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(login = c1.username)
        val actor = newCoreServiceActor()
        for {

          cc1 <- ask(actor, User.CreateUserCmd(c1))
          cc2 <- ask(actor, User.CreateUserCmd(c2))
        } yield {
          cc1 shouldBe a[CreateUserSuccess]
          cc2 shouldBe a[LoginAlreadyExistsMsg]
        }
      }

    }

    "UpdateUserCmd" must {
      "update all data of user" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))

        val actor = newCoreServiceActor()

        val f1 = ask(actor, User.CreateUserCmd(c1))
          .mapTo[CreateUserSuccess]
          .map(_.x)
          .map(user => {
            UpdateUser(
              id = user.id,
              username = Some(c2.username),
              displayName = None,
              firstName = Some(c2.firstName),
              lastName = Some(c2.lastName),
              middleName = Some(c2.middleName),
              email = Some(c2.email),
              url = None,
              description = None,
              phone = Some(c2.phone),
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
          cc2 <- ask(actor, User.UpdateUserCmd(cc1))
          ccs <- ask(actor, User.GetUserById(cc1.id)).mapTo[User.UserOpt].map(_.maybeEntry.get)
        } yield ccs shouldBe a[User]
      }

      "should not update user if there are no email & phone & login" in {
        val c2 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))

        val actor = newCoreServiceActor()

        val f1 = ask(actor, User.CreateUserCmd(c2))
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
              // applications = None,
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
          cc2 <- ask(actor, User.UpdateUserCmd(cc1))
        } yield cc2 shouldBe a[LoginRequiredMsg]
      }

      "should not update user if email already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val actor = newCoreServiceActor()

        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1))
          cc2 <- ask(actor, User.CreateUserCmd(c2)).mapTo[CreateUserSuccess].map(_.x)
          cc3 <- ask(actor, User.UpdateUserCmd(UpdateUser(
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

      "should not update user if phone already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))

        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1))
          cc2 <- ask(actor, User.CreateUserCmd(c2)).mapTo[CreateUserSuccess].map(_.x)
          cc3 <- ask(actor, User.UpdateUserCmd(UpdateUser(
            id = cc2.id,
            username = None,
            displayName = None,
            firstName = Some(c2.firstName),
            lastName = Some(c2.lastName),
            middleName = Some(c2.middleName),
            email = None,
            url = None,
            description = None,
            phone = Some(c1.phone),
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
        } yield cc3 shouldBe a[PhoneAlreadyExistsMsg]
      }

      "should not update user if login already exists" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))

        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1))
          cc2 <- ask(actor, User.CreateUserCmd(c2)).mapTo[CreateUserSuccess].map(_.x)
          cc3 <- ask(actor, User.UpdateUserCmd(UpdateUser(
            id = cc2.id,
            username = Some(c1.username),
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
        } yield cc3 shouldBe a[LoginAlreadyExistsMsg]
      }

    }

    "DeleteUserCmd" must {
      "delete user" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val c2 = newCreateUser(email = Some("valery1@valery.com"), phone = Some("+7123451"), login = Some("valery1"))
        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc2 <- ask(actor, User.DeleteUserCmd(cc1.id))
          ccs <- ask(actor, User.ListUsers).mapTo[User.UsersMap].map(_.x)
        } yield ccs.size shouldBe 0
      }

      "should not delete if user not exists" in {
        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.DeleteUserCmd(UUID.randomUUID()))
        } yield cc1 shouldBe a[UserNotFoundMsg]
      }
    }

    "FindUserByLoginAndPassword" must {
      "find user for correct password" in {
        val c1 = newCreateUser(email = Some("   valery@valery.com   "), phone = Some("   +712345   "), login = Some("   valery   ")).copy(password = "abc")
        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc3 <- ask(actor, User.GetUserByLoginAndPassword(c1.phone.get.toUpperCase.trim + " ", "abc")).mapTo[User.UserOpt].map(_.maybeEntry.get)
          cc4 <- ask(actor, User.GetUserByLoginAndPassword(c1.username.get.toUpperCase.trim + " ", "abc")).mapTo[User.UserOpt].map(_.maybeEntry.get)
        } yield {
          cc3.id shouldBe cc1.id
          cc4.id shouldBe cc1.id
        }
      }

      "don't find user for incorrect password" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1))
          cc2 <- ask(actor, User.GetUserByLoginAndPassword(c1.email.get, "abc1")).mapTo[User.UserOpt].map(_.maybeEntry)
          cc3 <- ask(actor, User.GetUserByLoginAndPassword(c1.phone.get, "abc1")).mapTo[User.UserOpt].map(_.maybeEntry)
          cc4 <- ask(actor, User.GetUserByLoginAndPassword(c1.username.get, "abc1")).mapTo[User.UserOpt].map(_.maybeEntry)
        } yield {
          cc1 shouldBe a[CreateUserSuccess]
          cc2 shouldBe None
          cc3 shouldBe None
          cc4 shouldBe None

        }
      }

      "don't find user for incorrect login" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1))
          cc2 <- ask(actor, User.GetUserByLoginAndPassword("", "abc1")).mapTo[User.UserOpt].map(_.maybeEntry)
        } yield {
          cc1 shouldBe a[CreateUserSuccess]
          cc2 shouldBe None
        }
      }
    }

    "UpdatePasswordCmd" must {
      "update password" in {
        val c1 = newCreateUser(email = Some("valery@valery.com"), phone = Some("+712345"), login = Some("valery"))
        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.CreateUserCmd(c1)).mapTo[CreateUserSuccess].map(_.x)
          cc5 <- ask(actor, User.UpdatePasswordCmd(cc1.id, "abc1"))
          cc2 <- ask(actor, User.GetUserByLoginAndPassword(c1.email.get, "abc1")).mapTo[User.UserOpt].map(_.maybeEntry.get)
          cc3 <- ask(actor, User.GetUserByLoginAndPassword(c1.phone.get, "abc1")).mapTo[User.UserOpt].map(_.maybeEntry.get)
          cc4 <- ask(actor, User.GetUserByLoginAndPassword(c1.username.get, "abc1")).mapTo[User.UserOpt].map(_.maybeEntry.get)
        } yield {
          cc2.id shouldBe cc1.id
          cc3.id shouldBe cc1.id
          cc4.id shouldBe cc1.id
        }
      }

      "don't update password for non existing user" in {
        val id = UUID.randomUUID()
        val actor = newCoreServiceActor()
        for {
          cc1 <- ask(actor, User.UpdatePasswordCmd(id, "abc1"))
        } yield {
          cc1 shouldBe UserNotFoundMsg(id)
        }
      }

    }
  }

  "A CoreServiceActor with an OpenSessionActor" when receive {
    "CreateOpenSessionCmd" must {
      "create new OpenSession" in {
        val s1 = newOpenSession
        val s2 = newOpenSession
        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          c2 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s2))
          r <- ask(actor, OpenSessionService.FindAllOpenSessions).mapTo[OpenSessionService.OpenSessionSeq].map(_.entries)
        } yield {
          c1 shouldBe Done
          c2 shouldBe Done
          r.size shouldBe 2
        }
      }
    }
    "UpdateOpenSessionCmd" must {
      "update tenantId" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          tenantId = Some("EXXO"))
        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.tenantId))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some("EXXO")
        }
      }
      "update languageId" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          languageId = Some("RU"))
        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.languageId))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some("RU")
        }
      }
      "update applicationId" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          applicationId = Some("exxo"))
        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.applicationId))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some("exxo")
        }
      }
      "update rememberMe" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          rememberMe = Some(false))
        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.rememberMe))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some(false)
        }
      }
      "update tenantId, languageId, applicationId" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          tenantId = Some("EXXO"),
          languageId = Some("RU"),
          applicationId = Some("exxo"))
        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt]
            .map(_.maybeEntry.map(x => (x.tenantId, x.languageId, x.applicationId)))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          r shouldBe Some(("EXXO", "RU", "exxo"))
        }
      }
      "update lastOpTimestamp" in {
        val s1 = newOpenSession
        val upd = OpenSessionUpdate(
          id = s1.id,
          lastOpTimestamp = Some(DateTime.now))
        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))
          r <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id))
            .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry.map(_.lastOpTimestamp))
        } yield {
          c1 shouldBe Done
          u shouldBe Done
          println(upd.lastOpTimestamp)
          r shouldBe upd.lastOpTimestamp
        }
      }
    }
    "UpdateOpenSessionCmd with wrong id" must {
      "do nothing" in {
        val upd = OpenSessionUpdate(
          id = UUID.randomUUID(),
          rememberMe = Some(false))
        val actor = newCoreServiceActor()
        for {
          u <- ask(actor, OpenSessionService.UpdateOpenSessionCmd(upd))

        } yield {
          u shouldBe OpenSessionService.EntryNotFound
        }
      }

    }
    "DeleteOpenSessionCmd" must {
      "delete" in {
        val s1 = newOpenSession

        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, OpenSessionService.CreateOpenSessionCmd(s1))
          r1 <- ask(actor, OpenSessionService.FindOpenSessionById(s1.id)).mapTo[OpenSessionOpt].map(_.maybeEntry)
          d <- ask(actor, OpenSessionService.DeleteOpenSessionCmd(s1.id))
          r2 <- ask(actor, OpenSessionService.FindAllOpenSessions).mapTo[OpenSessionSeq].map(_.entries)

        } yield {
          c1 shouldBe Done
          r1 shouldBe Some(s1)
          d shouldBe Done
          r2.size shouldBe 0
        }
      }

    }
    "DeleteOpenSessionCmd with wrong id" must {
      "do nothing" in {
        val actor = newCoreServiceActor()
        for {
          d <- ask(actor, OpenSessionService.DeleteOpenSessionCmd(UUID.randomUUID()))
        } yield {
          d shouldBe OpenSessionService.EntryNotFound
        }
      }

    }
  }

  "A CoreServiceActor with a LastSessionActor" when receive {
    "StoreLastSessionCmd" must {
      "create new LastSession" in {
        val s1 = newLastSession
        val s2 = newLastSession
        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, LastSessionService.StoreLastSessionCmd(s1))
          c2 <- ask(actor, LastSessionService.StoreLastSessionCmd(s2))
          r <- ask(actor, LastSessionService.FindAllLastSessions).mapTo[LastSessionService.LastSessionSeq].map(_.entries)
        } yield {
          c1 shouldBe Done
          c2 shouldBe Done
          r.size shouldBe 2
        }
      }
    }
    "StoreLastSessionCmd with the same userId" must {
      "update" in {
        val s1 = newLastSession
        val s2 = newLastSession.copy(userId = s1.userId)
        val actor = newCoreServiceActor()
        for {
          c1 <- ask(actor, LastSessionService.StoreLastSessionCmd(s1))
          c2 <- ask(actor, LastSessionService.StoreLastSessionCmd(s2))
          r <- ask(actor, LastSessionService.FindAllLastSessions).mapTo[LastSessionService.LastSessionSeq].map(_.entries)
          s <- ask(actor, LastSessionService.FindLastSessionByUserId(s1.userId)).mapTo[LastSessionOpt].map(_.maybeEntry)
        } yield {
          c1 shouldBe Done
          c2 shouldBe Done
          r.size shouldBe 1
          s shouldBe Some(s2)
        }
      }
    }
  }
  "A CoreServiceActor with a SessionHistoryActor" when receive {
    "CreateSessionHistoryCmd" must {
      "create new SessionHistory" in {
        val s1 = newSessionHistory
        val s2 = newSessionHistory
        val actor = newSessionHistoryActor()
        for {
          c1 <- ask(actor, SessionHistoryService.CreateSessionHistoryCmd(s1))
          c2 <- ask(actor, SessionHistoryService.CreateSessionHistoryCmd(s2))
          r <- ask(actor, SessionHistoryService.FindAllSessionHistory)
            .mapTo[SessionHistoryService.SessionHistorySeq].map(_.entries)
        } yield {
          c1 shouldBe Done
          c2 shouldBe Done
          r.size shouldBe 2
        }
      }
    }
    "CreateSessionHistoryCmd with the same id" must {
      "do nothing" in {
        val s1 = newSessionHistory
        val s2 = newSessionHistory.copy(id = s1.id)
        val actor = newSessionHistoryActor()
        for {
          c1 <- ask(actor, SessionHistoryService.CreateSessionHistoryCmd(s1))
          c2 <- ask(actor, SessionHistoryService.CreateSessionHistoryCmd(s2))
          r <- ask(actor, SessionHistoryService.FindAllSessionHistory).mapTo[SessionHistoryService.SessionHistorySeq].map(_.entries)
          r1 <- ask(actor, SessionHistoryService.FindSessionHistoryById(s1.id))
            .mapTo[SessionHistoryService.SessionHistoryOpt].map(_.maybeEntry)
        } yield {
          c1 shouldBe Done
          c2 shouldBe SessionHistoryService.EntryAlreadyExists
          r.size shouldBe 1
          r1 shouldBe Some(s1)
        }
      }
    }
  }

}