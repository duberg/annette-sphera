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
import annette.core.domain.property.PropertyService
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewProperty { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newPropertyActor() = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(PropertyService.props(s"Property-$uuid"), s"property-$uuid")
  }

}
