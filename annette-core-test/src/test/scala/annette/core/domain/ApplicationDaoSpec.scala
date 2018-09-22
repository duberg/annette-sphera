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
import annette.core.domain.application.{ ApplicationAlreadyExists, ApplicationNotFound, ApplicationService }
import annette.core.domain.application.dao.ApplicationDao
import annette.core.domain.application.model.{ Application, ApplicationUpdate }
import annette.core.domain.language.LanguageService
import annette.core.domain.language.model.{ Language, LanguageUpdate }
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model._
import annette.core.test.PersistenceSpec

import scala.concurrent.Future

class ApplicationDaoSpec extends TestKit(ActorSystem("ApplicationDaoSpec"))
  with PersistenceSpec
  with NewApplication {

  private def newCoreServiceActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(CoreService.props, s"CoreService-$uuid")
  }

  def newApplicationDao(): ApplicationDao = {
    val coreServiceActor = newCoreServiceActor()
    new ApplicationDao(coreServiceActor)
  }

  "An ApplicationDao" when call {
    "create" must {
      "create new application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val dao = newApplicationDao()
        for {
          cc1 <- dao.create(c1)
          _ <- dao.create(c2)
          ccs <- dao.selectAll
        } yield {
          cc1 shouldBe ()
          ccs.size shouldBe 2

        }
      }
      "should not create new application if it already exists" in {
        val c1 = Application("App1", "app1", "APP1")
        val dao = newApplicationDao()
        for {

          cc1 <- dao.create(c1)
          cc2 <- recoverToExceptionIf[ApplicationAlreadyExists] { dao.create(c1) }
          ccs <- dao.selectAll

        } yield {
          cc1 shouldBe ()
          cc2.exceptionMessage.get("code") shouldBe Some("core.application.alreadyExists")
          ccs.size shouldBe 1
        }
      }
    }

    "update" must {
      "update application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP1")
        val dao = newApplicationDao()
        for {

          cc1 <- dao.create(c1)
          cc2 <- dao.update(ApplicationUpdate(Some(c2.name), Some(c2.code), c1.id))
          ccs <- dao.getById(c1.id)
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          ccs shouldBe Some(c2)
        }
      }
      "should not update application if it doesn't exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val dao = newApplicationDao()
        for {
          cc1 <- recoverToExceptionIf[ApplicationNotFound](dao.update(ApplicationUpdate(Some(c2.name), Some(c2.code), c1.id)))
        } yield {
          cc1.exceptionMessage.get("code") shouldBe Some("core.application.notFound")
        }
      }
    }

    "delete" must {
      "delete application" in {
        val c1 = Application("App1", "app1", "APP1")
        val c2 = Application("App2", "app2", "APP2")
        val dao = newApplicationDao()
        for {
          cc1 <- dao.create(c1)
          cc2 <- dao.create(c2)
          ccs <- dao.selectAll
          d1 <- dao.delete(c1.id)
          ccr <- dao.selectAll
        } yield {
          cc1 shouldBe ()
          cc2 shouldBe ()
          ccs.head shouldBe c1
          ccs.last shouldBe c2
          d1 shouldBe ()
          ccr.head shouldBe c2
          ccr.size shouldBe 1
        }
      }
      "should not delete application if it does not exist" in {
        val c1 = Application("App1", "app1", "APP1")
        val dao = newApplicationDao()
        for {
          d1 <- recoverToExceptionIf[ApplicationNotFound](dao.delete(c1.id))
        } yield {
          d1.exceptionMessage.get("code") shouldBe Some("core.application.notFound")
        }
      }
    }
  }
}