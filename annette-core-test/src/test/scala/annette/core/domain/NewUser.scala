package annette.core.domain
import java.util.UUID

import akka.testkit.TestKit
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.model.{ CreateUser, User }
import annette.core.security.verification.VerificationBus
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewUser { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newUserActor() = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(UserService.props(verificationBus = new VerificationBus), s"user-$uuid")
  }

  def newCreateUser(id: Option[User.Id] = None, email: Option[String] = None, phone: Option[String] = None, login: Option[String] = None) = {
    CreateUser(
      id = id,
      username = login,
      displayName = None,
      firstName = generateString(),
      lastName = generateString(),
      middleName = genStrOpt,
      gender = genStrOpt,
      email = email,
      url = None,
      description = None,
      phone = phone,
      language = genStrOpt,
      roles = None,
      password = generatePassword,
      avatarUrl = None,
      sphere = None,
      company = None,
      position = None,
      rank = None,
      additionalTel = None,
      additionalMail = None,
      meta = None,
      status = Some(1))
  }
}
