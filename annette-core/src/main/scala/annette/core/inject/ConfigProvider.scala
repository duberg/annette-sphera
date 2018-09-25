package annette.core.inject

import java.io.File

import com.google.inject.Provider
import com.typesafe.config.{ Config, ConfigFactory }

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
