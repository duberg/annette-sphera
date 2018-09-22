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
package annette.core.server

import akka.actor.{ ActorRef, ActorSystem }
import annette.core.CoreModule
import annette.core.domain.{ CoreService, InitCoreTables }
import annette.core.services.authentication.{ AuthenticationService, AuthenticationServiceProvider }
import com.google.inject.{ AbstractModule, Provides }
import com.google.inject.name.Names
import javax.inject.Named
import net.codingwell.scalaguice.ScalaModule

class AnnetteServerModule extends AbstractModule with ScalaModule {
  override def configure() = {
    bind(classOf[InitCoreTables]).asEagerSingleton()
    bind(classOf[CoreModule]).asEagerSingleton()
    bind[ActorRef].annotatedWith(Names.named(AuthenticationService.name)).toProvider(classOf[AuthenticationServiceProvider]).asEagerSingleton()
  }

  @Provides
  @Named("CoreService")
  def getCoreService(actorSystem: ActorSystem) = {
    actorSystem.actorOf(CoreService.props)
  }
}
