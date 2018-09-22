/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.config

import java.io.File

import com.google.inject.Provider
import com.typesafe.config.{ Config, ConfigFactory }

/**
 * Created by valery on 25.01.17.
 */
class ConfigProvider extends Provider[Config] {
  override def get() = {
    println("Working Directory = " + System.getProperty("user.dir"))

    val configFile = Option(System.getProperties().getProperty("config.file")).getOrElse("conf/application.conf")
    println(s"ConfigFile = $configFile")
    val myConfigFile = new File(configFile)
    val fileConfig = ConfigFactory.parseFile(myConfigFile)
    ConfigFactory.load(fileConfig)
  }
}
