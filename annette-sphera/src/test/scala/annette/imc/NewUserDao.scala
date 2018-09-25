//package annette.imc
//
//import java.time.ZonedDateTime
//import java.util.UUID
//
//import annette.core.CoreModule
//import annette.core.inject.AkkaModule
//import annette.core.domain.tenancy.model.User
//import annette.core.inject.AnnetteServerModule
//import annette.core.test.PersistenceSpec
//import com.google.inject.Guice
//
//import scala.concurrent.{Await, ExecutionContextExecutor, Future}
//import scala.concurrent.duration._
//import com.google.inject.Guice
//import com.typesafe.config.{Config, ConfigException}
//import net.codingwell.scalaguice.InjectorExtensions._
//
//trait NewUserDao { _: PersistenceSpec =>
//
//  val injector = Guice.createInjector(
//    new AkkaModule(),
//    new AnnetteServerModule())
//
//  val coreModule = injector.instance[CoreModule]
//
//  val userIdA: UUID = generateUUID
//  val userA = User(
//    id = userIdA,
//    username = genStrOpt,
//    name = None,
//    firstName = generateString(),
//    lastName = generateString(),
//    middleName = genStrOpt,
//    email = generateEmailOpt,
//    url = None,
//    description = None,
//    phone = genPhoneOpt,
//    locale = genStrOpt,
//    tenants = Set.empty,
//    applications = Map.empty,
//    roles = Map.empty,
//    password = generatePassword,
//    registeredDate = ZonedDateTime.now(),
//    avatarUrl = None,
//    sphere = None,
//    company = None,
//    position = None,
//    rank = None,
//    additionalTel = None,
//    additionalMail = None,
//    meta = Map.empty,
//    deactivated = false)
//
//  val userIdB: UUID = generateUUID
//  val userB = User(
//    id = userIdB,
//    username = genStrOpt,
//    name = None,
//    firstName = generateString(),
//    lastName = generateString(),
//    middleName = genStrOpt,
//    email = generateEmailOpt,
//    url = None,
//    description = None,
//    phone = genPhoneOpt,
//    locale = genStrOpt,
//    tenants = Set.empty,
//    applications = Map.empty,
//    roles = Map.empty,
//    password = generatePassword,
//    registeredDate = ZonedDateTime.now(),
//    avatarUrl = None,
//    sphere = None,
//    company = None,
//    position = None,
//    rank = None,
//    additionalTel = None,
//    additionalMail = None,
//    meta = Map.empty,
//    deactivated = false)
//
//  val userIdC: UUID = generateUUID
//  val userC = User(
//    id = userIdC,
//    username = genStrOpt,
//    name = None,
//    firstName = generateString(),
//    lastName = generateString(),
//    middleName = genStrOpt,
//    email = generateEmailOpt,
//    url = None,
//    description = None,
//    phone = genPhoneOpt,
//    locale = genStrOpt,
//    tenants = Set.empty,
//    applications = Map.empty,
//    roles = Map.empty,
//    password = generatePassword,
//    registeredDate = ZonedDateTime.now(),
//    avatarUrl = None,
//    sphere = None,
//    company = None,
//    position = None,
//    rank = None,
//    additionalTel = None,
//    additionalMail = None,
//    meta = Map.empty,
//    deactivated = false)
//
//
//  val userIdD: UUID = generateUUID
//  val userD = User(
//    id = userIdD,
//    username = genStrOpt,
//    name = None,
//    firstName = generateString(),
//    lastName = generateString(),
//    middleName = genStrOpt,
//    email = generateEmailOpt,
//    url = None,
//    description = None,
//    phone = genPhoneOpt,
//    locale = genStrOpt,
//    tenants = Set.empty,
//    applications = Map.empty,
//    roles = Map.empty,
//    password = generatePassword,
//    registeredDate = ZonedDateTime.now(),
//    avatarUrl = None,
//    sphere = None,
//    company = None,
//    position = None,
//    rank = None,
//    additionalTel = None,
//    additionalMail = None,
//    meta = Map.empty,
//    deactivated = false)
//
//  val users = Set(
//    userA,
//    userB,
//    userC,
//    userD)
//
//  val userIds = Set(
//    userIdA,
//    userIdB,
//    userIdC,
//    userIdD)
//
//  def userFullname(u: User): String = s"${u.lastName} ${u.firstName} ${u.middleName}"
//  def userShortname(u: User): String = s"${u.lastName} ${u.firstName.take(1)}.${u.middleName.take(1)}."
//
//  def initUsersDao(): Unit = {
//    implicit val ec: ExecutionContextExecutor = coreModule.system.dispatcher
//    val f = for {
//      x <- coreModule.userDao.create(gene)
//      y <- coreModule.userDao.create(userB)
//      z <- coreModule.userDao.create(userC, generateId)
//      q <- coreModule.userDao.create(userD, generateId)
//    } yield x
//    Await.result(f, 1 minute)
//    info("UsersDao initialized")
//  }
//
//  def getUsers: Future[Set[User]] = coreModule.userDao.selectAll.map(_.toSet)
//
//  def getUsers(x: Set[User.Id]): Future[Set[User]] =
//    Future.sequence(x.map(coreModule.userDao.getById))
//      .map(_.flatten)
//}
