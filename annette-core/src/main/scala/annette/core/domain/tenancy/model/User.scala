/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.tenancy.model

import java.util.UUID

/**
 * Created by valery on 16.12.16.
 */
/**
 * Cодержит основные реквизиты пользователя
 *
 *
 * @param firstname  Имя пользователя
 * @param middlename Отчество или второе имя пользователя (опционально)
 * @param lastname   Фамилия пользователя
 * @param email      Адрес электронной почты (опционально)
 * @param phone      Телефон (опционально)
 * @param id         Уникальный идентификатор пользователя
 */
case class User(
  lastname: String,
  firstname: String,
  middlename: String,
  email: Option[String] = None,
  phone: Option[String] = None,
  login: Option[String] = None,
  defaultLanguage: String = "RU",
  id: User.Id = UUID.randomUUID()) {
  def toUserRec(password: String) = {
    UserRec(
      lastname = lastname,
      firstname = firstname,
      middlename = middlename,
      email = email,
      phone = phone,
      login = login,
      defaultLanguage = defaultLanguage,
      password = password,
      id = id)
  }
}

case class UserUpdate(
  lastname: Option[String] = None,
  firstname: Option[String] = None,
  middlename: Option[String] = None,
  email: Option[Option[String]] = None,
  phone: Option[Option[String]] = None,
  login: Option[Option[String]] = None,
  defaultLanguage: Option[String] = None,
  id: User.Id)

object User {
  type Id = UUID
}

case class UserRec(
  lastname: String,
  firstname: String,
  middlename: String,
  email: Option[String],
  phone: Option[String],
  login: Option[String],
  defaultLanguage: String,
  password: String,
  id: User.Id) {
  def toUser = User(
    lastname = lastname,
    firstname = firstname,
    middlename = middlename,
    email = email,
    phone = phone,
    login = login,
    defaultLanguage = defaultLanguage,
    id = id)
}
