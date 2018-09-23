package annette.core.domain.tenancy.actor

import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence
import annette.core.persistence.Persistence.PersistentState
import org.mindrot.jbcrypt.BCrypt

case class UsersActorState(
                            users: Map[User.Id, User] = Map.empty,
                            emailIndex: Map[String, User.Id] = Map.empty,
                            phoneIndex: Map[String, User.Id] = Map.empty,
                            usernameIndex: Map[String, User.Id] = Map.empty,
                            userProperties: Map[UserProperty.Id, UserProperty] = Map.empty) extends PersistentState[UsersActorState] {

  def createUser(create: CreateUser): UsersActorState = {
    validateCreate(create)
    val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
    val newEmailIndex = create.email.map{ email => emailIndex + (email.trim.toLowerCase -> create.id)}.getOrElse(emailIndex)
    val newPhoneIndex = create.phone.map{ phone => phoneIndex + (phone.trim.toLowerCase -> create.id)}.getOrElse(phoneIndex)
    val newLoginIndex = create.username.map{ login => usernameIndex + (login.trim.toLowerCase -> create.id)}.getOrElse(usernameIndex)
    val userRec = create.toUserRec(hashedPassword)

    println(s"User created: $userRec")

    copy(
      users = users + (create.id -> userRec),
      emailIndex = newEmailIndex ,
      phoneIndex= newPhoneIndex,
      usernameIndex = newLoginIndex
    )
  }

  def cleanse(user: User) = user.copy(
    email = user.email.map(_.trim.toLowerCase),
    phone = user.phone.map(_.trim.toLowerCase),
    username = user.username.map(_.trim.toLowerCase),
  )

  def validateCreate(create: CreateUser): Unit = {
    // проверяем наличие mail'а, телефона или логина
    if (create.email.isEmpty && create.phone.isEmpty && create.username.isEmpty) throw new LoginRequired
    // проверяем существует ли в системе пользователь с таким же email'ом, телефоном или логином
    if (create.email.exists(email => emailIndex.get(email.trim.toLowerCase).isDefined)) throw new EmailAlreadyExists(create.email.get)
    if (create.phone.exists(phone => phoneIndex.get(phone.trim.toLowerCase).isDefined)) throw new PhoneAlreadyExists(create.phone.get)
    if (create.username.exists(login => usernameIndex.get(login.trim.toLowerCase).isDefined)) throw new LoginAlreadyExists(create.username.get)
    // проверяем существует ли пользователь с таким же id
    if (users.get(create.id).isDefined) throw new UserAlreadyExists(create.id)
  }


  def validateUpdate(entry: UpdateUser): UserRec = {
    users
      .get(entry.id)
      .map{
        user =>
          // проверяем что email уже существует
          if (entry.email.flatten.exists( email => emailIndex.get(email.trim.toLowerCase).exists(_ != entry.id) )) throw new EmailAlreadyExists(entry.email.get.get)
          // проверяем что phone уже существует
          if (entry.phone.flatten.exists( phone => phoneIndex.get(phone.trim.toLowerCase).exists(_ != entry.id) )) throw new PhoneAlreadyExists(entry.phone.get.get)
          // проверяем что login уже существует
          if (entry.login.flatten.exists( login => usernameIndex.get(login.trim.toLowerCase).exists(_ != entry.id) )) throw new LoginAlreadyExists(entry.login.get.get)

          // проверяем наличие mail'а, телефона или логина
          val email = entry.email.getOrElse(user.email)
          val phone = entry.phone.getOrElse(user.phone)
          val login = entry.login.getOrElse(user.login)
          //println(s"Validate Update: entry = $entry, user = $user email = $email, phone = $phone, login = $login")
          if (email.isEmpty && phone.isEmpty && login.isEmpty) throw new LoginRequired
          user
      }
      .getOrElse(throw new UserNotFound(entry.id))
  }

  def updateUser(entry: UpdateUser): UsersActorState = {
    val userRec = validateUpdate(entry)

    val newEmailIndex = entry.email.map {
      // удаляем старый email, если он существует
      case None =>
        userRec.email.map(oldEmail => emailIndex - oldEmail.trim.toLowerCase).getOrElse(emailIndex)
      // удаляем старый email, если он существует и добавляем новый
      case Some(newEmail) =>
        userRec.email.map(oldEmail => emailIndex - oldEmail.trim.toLowerCase).getOrElse(emailIndex) + (newEmail.trim.toLowerCase -> entry.id)
    }.getOrElse(emailIndex) // ничего не меняем

    val newPhoneIndex = entry.phone.map {
      // удаляем старый phone, если он существует
      case None =>
        userRec.phone.map(oldPhone => phoneIndex - oldPhone.trim.toLowerCase).getOrElse(phoneIndex)
      // удаляем старый phone, если он существует и добавляем новый
      case Some(newPhone) =>
        userRec.phone.map(oldPhone => phoneIndex - oldPhone.trim.toLowerCase).getOrElse(phoneIndex) + (newPhone.trim.toLowerCase -> entry.id)
    }.getOrElse(phoneIndex) // ничего не меняем

    val newLoginIndex = entry.login.map {
      // удаляем старый login, если он существует
      case None =>
        userRec.login.map(oldLogin => usernameIndex - oldLogin.trim.toLowerCase).getOrElse(usernameIndex)
      // удаляем старый login, если он существует и добавляем новый
      case Some(newLogin) =>
        userRec.login.map(oldLogin => usernameIndex - oldLogin.trim.toLowerCase).getOrElse(usernameIndex) + (newLogin.trim.toLowerCase -> entry.id)
    }.getOrElse(usernameIndex) // ничего не меняем


    val updatedEntry = userRec.copy(
      firstname = entry.firstname.getOrElse(userRec.firstname),
      lastname = entry.lastname.getOrElse(userRec.lastname),
      middlename = entry.middlename.getOrElse(userRec.middlename),
      email = entry.email.getOrElse(userRec.email),
      phone = entry.phone.getOrElse(userRec.phone),
      login = entry.login.getOrElse(userRec.login),
      defaultLanguage = entry.defaultLanguage.getOrElse(userRec.defaultLanguage),
    )
    copy(
      users = users + (entry.id -> updatedEntry),
      emailIndex = newEmailIndex,
      phoneIndex = newPhoneIndex,
      usernameIndex = newLoginIndex
    )

  }

  def deleteUser(id: User.Id): UsersActorState = {
    users.get(id).map {
      user =>
      val newEmailIndex = user.email.map{ email => emailIndex - email.trim.toLowerCase}.getOrElse(emailIndex)
      val newPhoneIndex = user.phone.map{ phone => phoneIndex - phone.trim.toLowerCase }.getOrElse(phoneIndex)
      val newLoginIndex = user.login.map{ login => usernameIndex - login.trim.toLowerCase }.getOrElse(usernameIndex)
      copy(users = users - id)
    }.getOrElse( throw new UserNotFound(id) )

  }

  def findUserById(id: User.Id): Option[User] = users.get(id).map(_.toUser)

  def findAllUsers: Map[User.Id, User] = users.map(r => r._1 -> r._2.toUser)

  def userExists(id: User.Id): Boolean = users.get(id).isDefined

  def updatePassword(userId: User.Id, password: String): UsersActorState = {
    users
      .get(userId)
      .map{
        userRec =>
          val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
          val newUserRec = userRec.copy(password = hashedPassword)
          copy(users = users + (userId -> newUserRec))
      }
      .getOrElse(throw new UserNotFound(userId))
  }

  def findUserByLoginAndPassword(login: String, password: String): Option[User] = {
    val cleanLogin = login.toLowerCase.trim
    println(cleanLogin)
    findUserId(cleanLogin)
      .flatMap{
        userId =>
          users.get(userId).map {
            case userRec if BCrypt.checkpw(password, userRec.password) =>
              Some(userRec.toUser)
            case _ =>
              println(s"can't find user: $userId, cleanLogin: $cleanLogin")
              None
          }.getOrElse(throw new PasswordConsistencyError(userId))
      }
  }

  private def findUserId(login: String): Option[User.Id] = {
    val cleanLogin = login.toLowerCase.trim
    emailIndex.get(cleanLogin).map(Some(_))
      .getOrElse(
        phoneIndex.get(cleanLogin).map(Some(_))
          .getOrElse(usernameIndex.get(cleanLogin))
      )
  }

  override def updated(event: Persistence.PersistentEvent) = {
    event match {
      case UserService.UserCreatedEvt(entry,  password) => createUser(entry,  password)
      case UserService.UserUpdatedEvt(entry) => updateUser(entry)
      case UserService.UserDeletedEvt(id) => deleteUser(id)
      case UserService.PasswordUpdatedEvt(userId,  password) => updatePassword(userId,  password)
    }
  }
}
