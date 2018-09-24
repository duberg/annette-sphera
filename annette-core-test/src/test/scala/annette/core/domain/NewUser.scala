package annette.core.domain
import java.time.ZonedDateTime
import java.util.UUID

import akka.testkit.TestKit
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.model.{ CreateUser, User }
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewUser { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newUserActor() = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(UserService.props(s"User-$uuid"), s"user-$uuid")
  }

  def newCreateUser(id: UUID = UUID.randomUUID(), email: Option[String] = None, phone: Option[String] = None, login: Option[String] = None) = {
    CreateUser(
      username = login,
      displayName = None,
      firstName = generateString(),
      lastName = generateString(),
      middleName = genStrOpt,
      email = email,
      url = None,
      description = None,
      phone = phone,
      locale = genStrOpt,
      //tenants = Set.empty,
      //applications = Map.empty,
      //roles = Map.empty,
      password = generatePassword,
      avatarUrl = None,
      sphere = None,
      company = None,
      position = None,
      rank = None,
      additionalTel = None,
      additionalMail = None,
      meta = Map.empty,
      deactivated = false)
  }
}
