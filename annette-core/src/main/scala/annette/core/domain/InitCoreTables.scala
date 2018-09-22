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
import javax.inject._

import akka.actor.ActorSystem
import akka.event.{ LogSource, Logging }
import akka.http.scaladsl.util.FastFuture
import annette.core.domain.application.ApplicationAlreadyExists
import annette.core.domain.application.dao.{ ApplicationDao, ApplicationDb }
import annette.core.domain.application.model.Application
import annette.core.domain.language.LanguageAlreadyExists
import annette.core.domain.language.dao.{ LanguageDao, LanguageDb }
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.dao._
import annette.core.domain.tenancy.model.{ Tenant, TenantUserRole, User }
import annette.core.domain.tenancy.{ EmailAlreadyExists, PhoneAlreadyExists, TenantAlreadyExists, UserAlreadyExists }
import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContextExecutor, Future }
import scala.util.Try

/**
 * Created by valery on 17.12.16.
 */
@Singleton
class InitCoreTables @Inject() (
  config: Config,
  tenancyDb: TenancyDb,
  userDao: UserDao,
  tenantDao: TenantDao,
  tenantUserDao: TenantUserDao,
  tenantUserRoleDao: TenantUserRoleDao,
  languageDb: LanguageDb,
  languageDao: LanguageDao,
  applicationDb: ApplicationDb,
  applicationDao: ApplicationDao,
  system: ActorSystem) {

  implicit val myLogSourceType: LogSource[InitCoreTables] = (a: InitCoreTables) => "InitCoreTables"

  val log = Logging(system, this)

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val initDbConf = config.getConfig("annette.core.initDb")

  val createSchema = Try {
    initDbConf.getBoolean("createSchema")
  }.toOption.getOrElse(false)

  if (createSchema) {
    log.debug("Creating schema")
    val future = initDb()
    Await.result(future, Duration.Inf)
    log.debug("Creating schema completed")
  }

  def initDb(): Future[Unit] = {

    for {
      _ <- createTables(initDbConf)
      _ <- load("languages", loadLanguages)
      _ <- load("applications", loadApplications)
      _ <- load("tenants", loadTenants)
      _ <- load("users", loadUsers)
    } yield ()

  }

  private def load(configKey: String, loadFunc: List[Config] => Future[Unit]): Future[Unit] = {
    Try {
      initDbConf.getConfigList(configKey).asScala.toList
    }
      .toOption
      .map(c => loadFunc(c))
      .getOrElse(FastFuture.successful(()))
  }

  private def createTables(initDb: Config): Future[Unit] = {

    val createFuture = for {
      res1 <- languageDb.createAsync()
      res2 <- tenancyDb.createAsync()
      res3 <- applicationDb.createAsync()
    } yield (res1, res2, res3)
    createFuture.failed.foreach {
      case th: Throwable =>
        log.error("Create table failure: ")
        th.printStackTrace()
    }
    createFuture
      .map(_ => ())
      .recover { case _ => () }

  }

  private def loadLanguages(list: List[Config]): Future[Unit] = {
    val languages = list.map {
      conf =>
        Language(
          name = opt(conf.getString("name")).getOrElse(""),
          id = opt(conf.getString("id")).getOrElse("ID"))
    }
    Future
      .traverse(languages) {
        lang =>
          languageDao.create(lang).map(_ => ()).recover {
            case _: LanguageAlreadyExists =>
              log.error(s"Failed to load $lang: already exist")
            case th =>
              log.error(s"Failed to load $lang")
              th.printStackTrace()
          }
      }
      .map(_ => ())
  }

  private def opt[B](code: => B) = Try {
    code
  }.toOption

  private def loadApplications(list: List[Config]) = {
    val data = list.map {
      conf =>
        Application(
          name = opt(conf.getString("name")).getOrElse(""),
          code = opt(conf.getString("code")).getOrElse("CODE"),
          id = opt(conf.getString("id")).getOrElse("ID"))
    }
    Future
      .traverse(data) {
        obj =>
          applicationDao.create(obj).map(_ => ()).recover {
            case _: ApplicationAlreadyExists =>
              log.error(s"Failed to load $obj: already exist")
            case th =>
              log.error(s"Failed to load $obj")
              th.printStackTrace()
          }
      }
      .map(_ => ())
  }

  private def loadTenants(list: List[Config]) = {
    val data = list.map {
      conf =>
        val applications = opt(conf.getStringList("applications").asScala.toList).getOrElse(Set.empty[String])
        val languages = opt(conf.getStringList("languages").asScala.toList).getOrElse(Set.empty[String])
        Tenant(
          name = opt(conf.getString("name")).getOrElse(""),
          applicationId = opt(conf.getString("applicationId")).getOrElse(applications.head),
          applications = applications.toSet,
          languageId = opt(conf.getString("languageId")).getOrElse(languages.head),
          languages = languages.toSet,
          id = opt(conf.getString("id")).getOrElse("ID"))
    }
    Future
      .traverse(data) {
        obj =>
          tenantDao.create(obj).map(_ => ()).recover {
            case _: TenantAlreadyExists =>
              log.error(s"Failed to load $obj: already exist")
            case th =>
              log.error(s"Failed to load $obj")
              th.printStackTrace()
          }
      }
      .map(_ => ())

  }

  private def loadUsers(list: List[Config]) = {
    val data = list.map {
      conf =>
        val tenantsConf = opt(conf.getConfigList("tenants").asScala.toList).get
        val tenantsAndRoles = tenantsConf.map {
          case tenantConf =>
            val tenant = opt(tenantConf.getString("tenant")).getOrElse("")
            val roles = opt(tenantConf.getStringList("roles").asScala.toSet).getOrElse(Set.empty)
            tenant -> roles
        }
        val user = User(
          firstname = opt(conf.getString("firstname")).getOrElse(""),
          middlename = opt(conf.getString("middlename")).getOrElse(""),
          lastname = opt(conf.getString("lastname")).getOrElse(""),
          email = opt(conf.getString("email")),
          phone = opt(conf.getString("phone")),
          id = opt(UUID.fromString(conf.getString("id"))).getOrElse(UUID.randomUUID()))
        val password = opt(conf.getString("password")).getOrElse("abc")
        (user, password, tenantsAndRoles)
    }
    Future
      .traverse(data) {
        case (user, password, tenantsAndRoles) =>
          userDao
            .create(user, password)
            .andThen {
              case a =>
                tenantsAndRoles.map {
                  case (tenant, roles) =>
                    tenantUserDao.create(tenant, user.id)
                }
            }
            .andThen {
              case _ =>
                tenantsAndRoles.map {
                  case (tenant, roles) =>
                    if (roles.nonEmpty) tenantUserRoleDao.store(TenantUserRole(tenant, user.id, roles))
                }
            }
            .recover {
              case _: UserAlreadyExists =>
                log.error(s"Failed to load $user: already exist")
              case _: EmailAlreadyExists =>
                log.error(s"Failed to load $user: already exist")
              case _: PhoneAlreadyExists =>
                log.error(s"Failed to load $user: already exist")
              case th =>
                log.error(s"Failed to load $user")
                th.printStackTrace()
            }
      }
      .map(_ => ())
  }

}
