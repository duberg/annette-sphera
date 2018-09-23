/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.services.authentication

import javax.inject._
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.Cluster
import akka.event.{ LogSource, Logging }
import akka.routing.FromConfig
import annette.core.domain.InitCoreTables
import annette.core.domain.application.dao.ApplicationDao
import annette.core.domain.language.dao.LanguageDao
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.dao.{ SessionDao, TenantDao, TenantUserDao }
import com.google.inject.Provider
import com.typesafe.config.Config

/**
 * Created by valery on 25.01.17.
 */
@Singleton
class AuthenticationServiceProvider @Inject() (
  system: ActorSystem,
  sessionDao: SessionDao,
  tenantDao: TenantDao,
  applicationDao: ApplicationDao,
  userDao: UserService,
  tenantUserDao: TenantUserDao,
  languageDao: LanguageDao,
  config: Config,
  initCoreTables: InitCoreTables) extends Provider[ActorRef] {

  implicit val myLogSourceType: LogSource[AuthenticationServiceProvider] = (a: AuthenticationServiceProvider) => "AuthenticationServiceProvider"

  val log = Logging(system, this)

  log.debug("AuthenticationServiceProvider")

  val clusterMode = config.getBoolean("annette.cluster")
  final val routerName = "AuthenticationServiceRouter"

  private val authenticationService: ActorRef = initAuthenticationService()

  def get = authenticationService

  private def initAuthenticationService() = {
    if (clusterMode) initClusterMode()
    else initSingleInstanceMode()
  }

  private def initClusterMode() = {
    log.debug("InitAuthenticationService: Cluster Mode")
    val cluster = Cluster(system)
    if (cluster.selfRoles.contains("core")) {
      initService()
    }
    initClusterServiceRouter()
  }

  private def initSingleInstanceMode() = {
    log.info("InitAuthenticationService: Single Instance Mode")
    initService()
  }

  private def initService() = {
    system.actorOf(
      AuthenticationService.props(
        sessionDao = sessionDao,
        tenantDao = tenantDao,
        applicationDao = applicationDao,
        userDao = userDao,
        tenantUserDao = tenantUserDao,
        languageDao = languageDao,
        config = config),
      AuthenticationService.name)
  }

  private def initClusterServiceRouter() = {
    system.actorOf(FromConfig.props(Props.empty), routerName)
  }

}
