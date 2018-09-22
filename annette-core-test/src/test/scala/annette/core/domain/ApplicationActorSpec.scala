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

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.application.ApplicationService
import annette.core.domain.application.model.{ Application, ApplicationUpdate }
import annette.core.test.PersistenceSpec

class ApplicationActorSpec extends TestKit(ActorSystem("ApplicationActorSpec"))
  with PersistenceSpec with NewApplication {

  "An ApplicationActor" when receive {
    "CreateApplicationCmd" must {
      "create new application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val a = newApplicationActor()
        for {
          cc1 <- ask(a, ApplicationService.CreateApplicationCmd(c1))
          cc2 <- ask(a, ApplicationService.CreateApplicationCmd(c2))
          ccs <- ask(a, ApplicationService.FindAllApplications).mapTo[ApplicationService.MultipleApplications].map(_.entries)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs(c1.id) shouldBe c1
          ccs(c2.id) shouldBe c2
        }
      }
      "should not create new application if it already exists" in {
        val c1 = Application("App1", "app1", "APP1")
        val a = newApplicationActor()
        for {

          cc1 <- ask(a, ApplicationService.CreateApplicationCmd(c1))
          cc2 <- ask(a, ApplicationService.CreateApplicationCmd(c1))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe ApplicationService.EntryAlreadyExists
        }
      }
    }

    "UpdateApplicationCmd" must {
      "update application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP1")
        val a = newApplicationActor()
        for {

          cc1 <- ask(a, ApplicationService.CreateApplicationCmd(c1))
          cc2 <- ask(a, ApplicationService.UpdateApplicationCmd(ApplicationUpdate(Some(c2.name), Some(c2.code), c1.id)))
          ccs <- ask(a, ApplicationService.FindApplicationById(c1.id)).mapTo[ApplicationService.SingleApplication].map(_.maybeEntry)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs shouldBe Some(c2)
        }
      }
      "should not update application if it doesn't exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val a = newApplicationActor()
        for {
          cc1 <- ask(a, ApplicationService.UpdateApplicationCmd(ApplicationUpdate(Some(c2.name), Some(c2.code), c1.id)))
        } yield {
          cc1 shouldBe ApplicationService.EntryNotFound
        }
      }
    }

    "DeleteApplicationCmd" must {
      "delete application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val a = newApplicationActor()
        for {
          cc1 <- ask(a, ApplicationService.CreateApplicationCmd(c1))
          cc2 <- ask(a, ApplicationService.CreateApplicationCmd(c2))
          ccs <- ask(a, ApplicationService.FindAllApplications).mapTo[ApplicationService.MultipleApplications].map(_.entries)
          d1 <- ask(a, ApplicationService.DeleteApplicationCmd(c1.id))
          ccr <- ask(a, ApplicationService.FindAllApplications).mapTo[ApplicationService.MultipleApplications].map(_.entries)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs(c1.id) shouldBe c1
          ccs(c2.id) shouldBe c2
          d1 shouldBe Done
          ccr(c2.id) shouldBe c2
          ccr.size shouldBe 1
        }
      }
      "should not delete application if it does not exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val a = newApplicationActor()
        for {
          d1 <- ask(a, ApplicationService.DeleteApplicationCmd(c1.id))
        } yield {
          d1 shouldBe ApplicationService.EntryNotFound
        }
      }
    }

  }
}