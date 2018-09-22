package annette.imc

import java.util.UUID

import annette.core.CoreModule
import annette.core.akkaguice.AkkaModule
import annette.core.domain.tenancy.model.User
import annette.core.server.AnnetteServerModule
import annette.core.test.PersistenceSpec
import com.google.inject.Guice

import scala.concurrent.{ Await, ExecutionContextExecutor, Future }
import scala.concurrent.duration._
import com.google.inject.Guice
import com.typesafe.config.{ Config, ConfigException }
import net.codingwell.scalaguice.InjectorExtensions._

trait NewUserDao { _: PersistenceSpec =>

  val injector = Guice.createInjector(
    new AkkaModule(),
    new AnnetteServerModule())

  val coreModule = injector.instance[CoreModule]

  val userIdA: UUID = generateUUID
  val userA = User(
    lastname = "Дзуцев",
    firstname = "Игорь",
    middlename = "Дмитриевич",
    email = generateEmailOpt,
    phone = None,
    login = None,
    defaultLanguage = "RU",
    userIdA)

  val userIdB: UUID = generateUUID
  val userB = User(
    lastname = "Бородина",
    firstname = "Светлана",
    middlename = s"Александровна",
    email = generateEmailOpt,
    phone = None,
    login = None,
    defaultLanguage = "EN",
    userIdB)

  val userIdC: UUID = generateUUID
  val userC = User(
    lastname = "Бородулин",
    firstname = "Вячеслав",
    middlename = "Владимирович",
    email = generateEmailOpt,
    phone = None,
    login = None,
    defaultLanguage = "EN",
    userIdC)

  val userIdD: UUID = generateUUID
  val userD = User(
    lastname = "Кириенко",
    firstname = "Владимир",
    middlename = "Сергеевич",
    email = generateEmailOpt,
    phone = None,
    login = None,
    defaultLanguage = "EN",
    userIdD)

  val users = Set(
    userA,
    userB,
    userC,
    userD)

  val userIds = Set(
    userIdA,
    userIdB,
    userIdC,
    userIdD)

  def userFullname(u: User): String = s"${u.lastname} ${u.firstname} ${u.middlename}"
  def userShortname(u: User): String = s"${u.lastname} ${u.firstname.take(1)}.${u.middlename.take(1)}."

  def initUsersDao(): Unit = {
    implicit val ec: ExecutionContextExecutor = coreModule.system.dispatcher
    val f = for {
      x <- coreModule.userDao.create(userA, generateId)
      y <- coreModule.userDao.create(userB, generateId)
      z <- coreModule.userDao.create(userC, generateId)
      q <- coreModule.userDao.create(userD, generateId)
    } yield x
    Await.result(f, 1 minute)
    info("UsersDao initialized")
  }

  def getUsers: Future[Set[User]] = coreModule.userDao.selectAll.map(_.toSet)

  def getUsers(x: Set[User.Id]): Future[Set[User]] =
    Future.sequence(x.map(coreModule.userDao.getById))
      .map(_.flatten)
}
