/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.tenancy

import annette.core.domain.tenancy.model.User
import annette.core.exception.{ AnnetteMessage, AnnetteMessageException }

/**
 * Created by valery on 17.12.16.
 */
case class LoginRequiredMsg() extends AnnetteMessage("core.tenancy.user.loginRequired") {
  override def toException = new LoginRequired()
}
class LoginRequired extends AnnetteMessageException(LoginRequiredMsg())

case class UserAlreadyExistsMsg(id: User.Id) extends AnnetteMessage("core.tenancy.user.alreadyExists", Map("userId" -> id.toString)) {
  override def toException = new UserAlreadyExists(id)
}

class UserAlreadyExists(id: User.Id) extends AnnetteMessageException(UserAlreadyExistsMsg(id))

case class EmailAlreadyExistsMsg(email: String) extends AnnetteMessage("core.tenancy.user.emailAlreadyExists", Map("email" -> email)) {
  override def toException = new EmailAlreadyExists(email)
}
class EmailAlreadyExists(email: String) extends AnnetteMessageException(EmailAlreadyExistsMsg(email))

case class PhoneAlreadyExistsMsg(phone: String) extends AnnetteMessage("core.tenancy.user.phoneAlreadyExists", Map("phone" -> phone)) {
  override def toException = new PhoneAlreadyExists(phone)
}
class PhoneAlreadyExists(phone: String) extends AnnetteMessageException(PhoneAlreadyExistsMsg(phone))

case class LoginAlreadyExistsMsg(login: String) extends AnnetteMessage("core.tenancy.user.loginAlreadyExists", Map("phone" -> login)) {
  override def toException = new LoginAlreadyExists(login)
}
class LoginAlreadyExists(login: String) extends AnnetteMessageException(LoginAlreadyExistsMsg(login))

case class UserNotFoundMsg(id: User.Id) extends AnnetteMessage("core.tenancy.user.notFound", Map("userId" -> id.toString)) {
  override def toException = new UserNotFound(id)
}
class UserNotFound(id: User.Id) extends AnnetteMessageException(UserNotFoundMsg(id))

case class PasswordConsistencyErrorMsg(id: User.Id) extends AnnetteMessage("core.tenancy.user.passwordConsistencyError", Map("userId" -> id.toString)) {
  override def toException = new PasswordConsistencyError(id)
}
class PasswordConsistencyError(id: User.Id) extends AnnetteMessageException(PasswordConsistencyErrorMsg(id))

case class UserEmailPasswordConsistencyErrorMsg(user: User) extends AnnetteMessage(
  "core.tenancy.user.emailPasswordConsistencyError",
  Map("userId" -> user.id.toString, "firstname" -> user.firstname, "lastname" -> user.lastname)) {
  override def toException = new UserEmailPasswordConsistencyError(user)
}
class UserEmailPasswordConsistencyError(user: User) extends AnnetteMessageException(UserEmailPasswordConsistencyErrorMsg(user))

case class UserPhonePasswordConsistencyErrorMsg(user: User) extends AnnetteMessage(
  "core.tenancy.user.phonePasswordConsistencyError",
  Map("userId" -> user.id.toString, "firstname" -> user.firstname, "lastname" -> user.lastname)) {
  override def toException = new UserPhonePasswordConsistencyError(user)
}
class UserPhonePasswordConsistencyError(user: User) extends AnnetteMessageException(UserPhonePasswordConsistencyErrorMsg(user))

case class UserLoginPasswordConsistencyErrorMsg(user: User) extends AnnetteMessage(
  "core.tenancy.user.loginPasswordConsistencyError",
  Map("userId" -> user.id.toString, "firstname" -> user.firstname, "lastname" -> user.lastname)) {
  override def toException = new UserLoginPasswordConsistencyError(user)
}
class UserLoginPasswordConsistencyError(user: User) extends AnnetteMessageException(UserLoginPasswordConsistencyErrorMsg(user))

case class UserConsistencyErrorMsg(user: User) extends AnnetteMessage(
  "core.tenancy.user.consistencyError",
  Map("userId" -> user.id.toString, "firstname" -> user.firstname, "lastname" -> user.lastname)) {
  override def toException = new UserConsistencyError(user)
}
class UserConsistencyError(user: User) extends AnnetteMessageException(UserConsistencyErrorMsg(user))

