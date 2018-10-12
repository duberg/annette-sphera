package annette.core.domain

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.application.ApplicationService
import annette.core.domain.application._
import annette.core.test.PersistenceSpec

class ApplicationActorSpec extends TestKit(ActorSystem("ApplicationActorSpec"))
  with PersistenceSpec with NewApplication {

  "An ApplicationActor" when receive {
    "CreateApplicationCmd" must {
      "createUser new application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val a = newApplicationActor()
        for {
          cc1 <- ask(a, Application.CreateApplicationCmd(c1))
          cc2 <- ask(a, Application.CreateApplicationCmd(c2))
          ccs <- ask(a, Application.ListApplications).mapTo[Application.ApplicationsMap].map(_.x)
        } yield {
          //cc1 shouldBe Done
          //cc2 shouldBe Done
          ccs(c1.id) shouldBe c1
          ccs(c2.id) shouldBe c2
        }
      }
      "should not createUser new application if it already exists" in {
        val c1 = Application("App1", "app1", "APP1")
        val a = newApplicationActor()
        for {

          cc1 <- ask(a, Application.CreateApplicationCmd(c1))
          cc2 <- ask(a, Application.CreateApplicationCmd(c1))
        } yield {
          //cc1 shouldBe Done
          cc2 shouldBe Application.EntryAlreadyExists
        }
      }
    }

    "UpdateApplicationCmd" must {
      "updateUser application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP1")
        val a = newApplicationActor()
        for {

          cc1 <- ask(a, Application.CreateApplicationCmd(c1))
          cc2 <- ask(a, Application.UpdateApplicationCmd(UpdateApplication(Some(c2.name), Some(c2.code), c1.id)))
          ccs <- ask(a, Application.GetApplicationById(c1.id)).mapTo[Application.ApplicationOpt].map(_.x)
        } yield {
          // cc1 shouldBe Done
          //cc2 shouldBe Done
          ccs shouldBe Some(c2)
        }
      }
      "should not updateUser application if it doesn't exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val a = newApplicationActor()
        for {
          cc1 <- ask(a, Application.UpdateApplicationCmd(UpdateApplication(Some(c2.name), Some(c2.code), c1.id)))
        } yield {
          cc1 shouldBe Application.EntryNotFound
        }
      }
    }

    "DeleteApplicationCmd" must {
      "deleteUser application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val a = newApplicationActor()
        for {
          cc1 <- ask(a, Application.CreateApplicationCmd(c1))
          cc2 <- ask(a, Application.CreateApplicationCmd(c2))
          ccs <- ask(a, Application.ListApplications).mapTo[Application.ApplicationsMap].map(_.x)
          d1 <- ask(a, Application.DeleteApplicationCmd(c1.id))
          ccr <- ask(a, Application.ListApplications).mapTo[Application.ApplicationsMap].map(_.x)
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
      "should not deleteUser application if it does not exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val a = newApplicationActor()
        for {
          d1 <- ask(a, Application.DeleteApplicationCmd(c1.id))
        } yield {
          d1 shouldBe Application.EntryNotFound
        }
      }
    }

  }
}