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
import annette.core.domain.language.LanguageService
import annette.core.domain.language.model.{ Language, LanguageUpdate }
import annette.core.test.PersistenceSpec

class LanguageActorSpec extends TestKit(ActorSystem("LanguageActorSpec"))
  with PersistenceSpec with NewLanguage {

  "A LanguageActor" when receive {
    "CreateLanguageCmd" must {
      "create new language" in {
        val c1 = Language("English", "EN")
        val c2 = Language("Russian", "RU")
        val a = newLanguageActor()
        for {
          cc1 <- ask(a, LanguageService.CreateLanguageCmd(c1))
          cc2 <- ask(a, LanguageService.CreateLanguageCmd(c2))
          ccs <- ask(a, LanguageService.FindAllLanguages).mapTo[LanguageService.MultipleLanguages].map(_.entries)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs(c1.id) shouldBe c1
          ccs(c2.id) shouldBe c2
        }
      }
      "should not create new language if it already exists" in {
        val c1 = Language("English", "EN")
        val a = newLanguageActor()
        for {

          cc1 <- ask(a, LanguageService.CreateLanguageCmd(c1))
          cc2 <- ask(a, LanguageService.CreateLanguageCmd(c1))
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe LanguageService.EntryAlreadyExists
        }
      }
    }

    "UpdateLanguageCmd" must {
      "update language" in {
        val c1 = Language("English", "EN")
        val c2 = Language("English-US", "EN")
        val a = newLanguageActor()
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
        val a = newLanguageActor()
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
        val a = newLanguageActor()
        for {
          cc1 <- ask(a, LanguageService.CreateLanguageCmd(c1))
          cc2 <- ask(a, LanguageService.CreateLanguageCmd(c2))
          ccs <- ask(a, LanguageService.FindAllLanguages).mapTo[LanguageService.MultipleLanguages].map(_.entries)
          d1 <- ask(a, LanguageService.DeleteLanguageCmd(c1.id))
          ccr <- ask(a, LanguageService.FindAllLanguages).mapTo[LanguageService.MultipleLanguages].map(_.entries)
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
      "should not delete language if it does not exist" in {
        val c1 = Language("English", "EN")
        val a = newLanguageActor()
        for {
          d1 <- ask(a, LanguageService.DeleteLanguageCmd(c1.id))
        } yield {
          d1 shouldBe LanguageService.EntryNotFound
        }
      }
    }

  }
}