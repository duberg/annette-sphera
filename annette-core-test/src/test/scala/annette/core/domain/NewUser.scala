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
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.model.User
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewUser { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newUserActor() = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(UserService.props(s"User-$uuid"), s"user-$uuid")
  }

  def newUser(id: UUID = UUID.randomUUID(), email: Option[String] = None, phone: Option[String] = None, login: Option[String] = None) = {
    User(
      lastname = s"Lastname-${random.nextInt(100)}",
      firstname = s"Firstname-${random.nextInt(100)}",
      middlename = s"Middlename-${random.nextInt(100)}",
      email = email,
      phone = phone,
      username = login,
      defaultLanguage = s"Language-${random.nextInt(100)}",
      id = id)
  }
}
