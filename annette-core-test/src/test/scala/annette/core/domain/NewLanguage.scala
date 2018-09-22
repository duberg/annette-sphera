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

import akka.testkit.TestKit
import annette.core.domain.language.LanguageService
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewLanguage { _: PersistenceSpec with TestKit =>
  private val random = new Random()

  def newLanguageActor() = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(LanguageService.props(s"Language-$uuid"), s"language--$uuid")
  }
}
