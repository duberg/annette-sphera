
package annette.core.domain

import java.net.InetSocketAddress

import javax.inject._
import com.datastax.driver.core._
import com.outworkers.phantom.connectors.KeySpaceBuilder
import com.typesafe.config.Config
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

import scala.collection.JavaConverters._
import slick.jdbc.PostgresProfile.api._

@Singleton
class DB @Inject() (config: Config) {
  //  val hosts = config.getStringList("annette.cassandra.hosts").asScala.map {
  //    hostAndPort =>
  //      val arr = hostAndPort.split(":")
  //      if (arr.length > 1) new InetSocketAddress(arr(0), arr(1).toInt)
  //      else new InetSocketAddress(arr(0), 9042)
  //  }.toList
  //  val keyspace = config.getString("annette.cassandra.keyspace")
  //  val user = config.getString("annette.cassandra.user")
  //  val password = config.getString("annette.cassandra.password")
  //
  //  val clusterBuilder = Cluster
  //    .builder()
  //    .addContactPointsWithPorts(hosts: _*)
  //    .withAuthProvider(new PlainTextAuthProvider(user, password))
  //
  //  val keySpaceDef = new KeySpaceBuilder(_ => clusterBuilder).keySpace(keyspace)
  // ContactPoints(host, port).keySpace(keyspace)

  val db: PostgresProfile.backend.Database = Database.forConfig("slick", config)
}

