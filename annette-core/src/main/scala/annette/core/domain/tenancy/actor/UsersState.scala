package annette.core.domain.tenancy.actor

import java.time.ZonedDateTime

import annette.core.akkaext.actor.{ CqrsEvent, CqrsState }
import annette.core.domain.application.Application
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.model.User._
import org.mindrot.jbcrypt.BCrypt

case class UsersState(
  users: Map[User.Id, User] = Map.empty,
  emailIndex: Map[String, User.Id] = Map.empty,
  phoneIndex: Map[String, User.Id] = Map.empty,
  usernameIndex: Map[String, User.Id] = Map.empty,
  userProperties: Map[UserProperty.Id, UserProperty] = Map.empty) extends CqrsState {

  def createUser(x: User): UsersState = {
    val newEmailIndex = x.email.map { email => emailIndex + (email.trim.toLowerCase -> x.id) }.getOrElse(emailIndex)
    val newPhoneIndex = x.phone.map { phone => phoneIndex + (phone.trim.toLowerCase -> x.id) }.getOrElse(phoneIndex)
    val newLoginIndex = x.username.map { login => usernameIndex + (login.trim.toLowerCase -> x.id) }.getOrElse(usernameIndex)

    copy(
      users = users + (x.id -> x),
      emailIndex = newEmailIndex,
      phoneIndex = newPhoneIndex,
      usernameIndex = newLoginIndex)
  }

  def cleanse(user: User) = user.copy(
    email = user.email.map(_.trim.toLowerCase),
    phone = user.phone.map(_.trim.toLowerCase),
    username = user.username.map(_.trim.toLowerCase))

  def validateCreate(create: CreateUser): Unit = {
    // проверяем наличие mail'а, телефона или логина
    if (create.email.isEmpty && create.phone.isEmpty && create.username.isEmpty) throw new LoginRequired
    // проверяем существует ли в системе пользователь с таким же email'ом, телефоном или логином
    if (create.email.exists(email => emailIndex.get(email.trim.toLowerCase).isDefined)) throw new EmailAlreadyExists(create.email.get)
    if (create.phone.exists(phone => phoneIndex.get(phone.trim.toLowerCase).isDefined)) throw new PhoneAlreadyExists(create.phone.get)
    if (create.username.exists(login => usernameIndex.get(login.trim.toLowerCase).isDefined)) throw new LoginAlreadyExists(create.username.get)
    // проверяем существует ли пользователь с таким же id
    //if (users.get(create.id).isDefined) throw new UserAlreadyExists(create.id)
  }

  def validateUpdate(x: UpdateUser): User = {
    users
      .get(x.id)
      .map {
        user =>
          // проверяем что email уже существует
          if (x.email.flatten.exists(email => emailIndex.get(email.trim.toLowerCase).exists(_ != x.id))) throw new EmailAlreadyExists(x.email.get.get)
          // проверяем что phone уже существует
          if (x.phone.flatten.exists(phone => phoneIndex.get(phone.trim.toLowerCase).exists(_ != x.id))) throw new PhoneAlreadyExists(x.phone.get.get)
          // проверяем что login уже существует
          if (x.username.flatten.exists(username => usernameIndex.get(username.trim.toLowerCase).exists(_ != x.id))) throw new LoginAlreadyExists(x.username.get.get)

          // проверяем наличие mail'а, телефона или логина
          val email = x.email.getOrElse(user.email)
          val phone = x.phone.getOrElse(user.phone)
          val username = x.username.getOrElse(user.username)
          //println(s"Validate Update: entry = $entry, user = $user email = $email, phone = $phone, login = $login")
          if (email.isEmpty && phone.isEmpty && username.isEmpty) throw new LoginRequired
          user
      }
      .getOrElse(throw new UserNotFound(x.id))
  }

  def updateUser(x: UpdateUser): UsersState = {
    val user = validateUpdate(x)

    val newEmailIndex = x.email.map {
      // удаляем старый email, если он существует
      case None =>
        user.email.map(oldEmail => emailIndex - oldEmail.trim.toLowerCase).getOrElse(emailIndex)
      // удаляем старый email, если он существует и добавляем новый
      case Some(newEmail) =>
        user.email.map(oldEmail => emailIndex - oldEmail.trim.toLowerCase).getOrElse(emailIndex) + (newEmail.trim.toLowerCase -> x.id)
    }.getOrElse(emailIndex) // ничего не меняем

    val newPhoneIndex = x.phone.map {
      // удаляем старый phone, если он существует
      case None =>
        user.phone.map(oldPhone => phoneIndex - oldPhone.trim.toLowerCase).getOrElse(phoneIndex)
      // удаляем старый phone, если он существует и добавляем новый
      case Some(newPhone) =>
        user.phone.map(oldPhone => phoneIndex - oldPhone.trim.toLowerCase).getOrElse(phoneIndex) + (newPhone.trim.toLowerCase -> x.id)
    }.getOrElse(phoneIndex) // ничего не меняем

    val newUsernameIndex = x.username.map {
      // удаляем старый login, если он существует
      case None =>
        user.username.map(oldUsername => usernameIndex - oldUsername.trim.toLowerCase).getOrElse(usernameIndex)
      // удаляем старый login, если он существует и добавляем новый
      case Some(newLogin) =>
        user.username.map(oldUsername => usernameIndex - oldUsername.trim.toLowerCase).getOrElse(usernameIndex) + (newLogin.trim.toLowerCase -> x.id)
    }.getOrElse(usernameIndex) // ничего не меняем

    val updated = user.copy(
      username = x.username.getOrElse(user.username),
      firstName = x.firstName.getOrElse(user.firstName),
      lastName = x.lastName.getOrElse(user.lastName),
      middleName = x.middleName.getOrElse(user.middleName),
      email = x.email.getOrElse(user.email),
      phone = x.phone.getOrElse(user.phone),
      language = x.language.getOrElse(user.language))

    copy(
      users = users + (x.id -> updated),
      emailIndex = newEmailIndex,
      phoneIndex = newPhoneIndex,
      usernameIndex = newUsernameIndex)
  }

  def deleteUser(id: User.Id): UsersState = {
    users.get(id).map {
      user =>
        val newEmailIndex = user.email.map { email => emailIndex - email.trim.toLowerCase }.getOrElse(emailIndex)
        val newPhoneIndex = user.phone.map { phone => phoneIndex - phone.trim.toLowerCase }.getOrElse(phoneIndex)
        val newLoginIndex = user.username.map { username => usernameIndex - username.trim.toLowerCase }.getOrElse(usernameIndex)
        copy(users = users - id)
    }.getOrElse(throw new UserNotFound(id))

  }

  def findUserById(id: User.Id): Option[User] = users.get(id)

  def userExists(id: User.Id): Boolean = users.get(id).isDefined

  def updatePassword(userId: User.Id, password: String): UsersState = {
    users
      .get(userId)
      .map {
        userRec =>
          val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
          val newUserRec = userRec.copy(password = hashedPassword)
          copy(users = users + (userId -> newUserRec))
      }
      .getOrElse(throw new UserNotFound(userId))
  }

  def findUserByLoginAndPassword(login: String, password: String): Option[User] = {
    val cleanLogin = login.toLowerCase.trim
    findUserId(cleanLogin)
      .flatMap {
        userId =>
          users.get(userId).map {
            case user if BCrypt.checkpw(password, user.password) =>
              Some(user)
            case _ =>
              println(s"can't find user: $userId, cleanLogin: $cleanLogin")
              None
          }.getOrElse(throw new PasswordConsistencyError(userId))
      }
  }

  def findUserByEmail(email: String): Option[User] = emailIndex.get(email).flatMap(findUserById)

  private def findUserId(login: String): Option[User.Id] = {
    val cleanLogin = login.toLowerCase.trim
    emailIndex.get(cleanLogin).map(Some(_))
      .getOrElse(
        phoneIndex.get(cleanLogin).map(Some(_))
          .getOrElse(usernameIndex.get(cleanLogin)))
  }

  def activateUser(x: User.Id): UsersState = {
    val user = users(x).copy(status = 1)
    copy(users = users + (user.id -> user))
  }

  def update: Update = {
    case CreatedUserEvt(x) => createUser(x)
    case UpdatedUserEvt(x) => updateUser(x)
    case DeletedUserEvt(x) => deleteUser(x)
    case UpdatedPasswordEvt(x, y) => updatePassword(x, y)
    case ActivatedUserEvt(x) => activateUser(x)
  }
}
