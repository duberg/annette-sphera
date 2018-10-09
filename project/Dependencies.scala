import sbt._

object Dependencies {
  object Version {
    val scala = "2.12.4"
    val akka = "2.5.16"
    val akkaPersistenceCassandra = "0.90"
    //val akkaPersistencePostgresql = "0.10.0"
    val akkaPersistenceInmemoryVersion = "2.5.0.0"
    val akkaHttp = "10.0.14"
    val phantom = "2.12.1"
    val protobuf = "3.2.0"
    val guice = "4.1.0"
    val jwt = "1.2.2"
    val akkaHttpCircle = "1.22.0"
    val circle = "0.10.0"
    val scalaj = "2.3.0"
    val scalamock = "3.6.0"
    val apachePoi = "3.17"
    val casbin = "1.1.0"
    val shapeless = "2.3.3"
    val monocle = "1.5.0"
  }

  //lazy val akkaBackend: Seq[sbt.ModuleID] = common ++ tests ++ guice ++ reflect //++ metrics
  lazy val core: Seq[ModuleID] = Seq (
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster-metrics" % Version.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Version.akka,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
   // "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
    "com.typesafe.akka" %% "akka-persistence" % Version.akka,
    "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.akkaPersistenceCassandra,

    "com.okumin" %% "akka-persistence-sql-async" % "0.5.1",
    "com.github.mauricio" %% "postgresql-async" % "0.2.21",

    "com.typesafe.akka" %% "akka-http"         % Version.akkaHttp,
    "com.typesafe.akka" %% "akka-http-xml"     % Version.akkaHttp,
    "com.typesafe.akka" %% "akka-stream"       % Version.akka,
    "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % Test,
    "io.igl" %% "jwt" % Version.jwt,
    "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpCircle,
    "io.circe" %% "circe-core" % Version.circle,
    "io.circe" %% "circe-generic" % Version.circle,
    "io.circe" %% "circe-parser" % Version.circle,
    "io.circe" %% "circe-java8" % Version.circle,
    "com.google.protobuf" % "protobuf-java" % Version.protobuf,
    "javax.inject" % "javax.inject" % "1",
    "com.outworkers" %% "phantom-dsl" % Version.phantom,
    "org.mindrot" % "jbcrypt" % "0.3m",
    "com.carrotsearch" % "java-sizeof" % "0.0.5",
    "com.sun.mail" % "javax.mail" % "1.6.0",
    "org.scalaj" %% "scalaj-http" % Version.scalaj,
    "com.roundeights" %% "hasher" % "1.2.0",
    "org.scalamock" %% "scalamock-scalatest-support" % Version.scalamock % Test,
    "org.casbin" % "jcasbin" % Version.casbin,

    "com.chuusai" %% "shapeless" % Version.shapeless,

    "com.github.julien-truffaut" %%  "monocle-core"  % Version.monocle,
    "com.github.julien-truffaut" %%  "monocle-macro" % Version.monocle,
    "com.github.julien-truffaut" %%  "monocle-law"   % Version.monocle % "test"
  ) ++ test ++ guice ++ reflect

  /**
    * todo: Помещай зависимости в отдельные строки
    */
  val akkaTestKit: ModuleID = "com.typesafe.akka" %% "akka-testkit" % Version.akka
  val akkaPersistenceInmemory: ModuleID = "com.github.dnvriend" %% "akka-persistence-inmemory" % Version.akkaPersistenceInmemoryVersion

  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1"

  val commonsio: ModuleID = "commons-io" % "commons-io" % "2.5"

  val testInCompile: Seq[ModuleID] = Seq(
    scalatest,
    akkaTestKit,
    akkaPersistenceInmemory,
    commonsio
  )

  val test: Seq[ModuleID] = testInCompile.map(_ % Test)

  lazy val imc: Seq[sbt.ModuleID] = Seq(

    "com.sun.mail" % "javax.mail" % "1.6.0",

    "org.scalaj" %% "scalaj-http" % Version.scalaj,

    "org.scalamock" %% "scalamock-scalatest-support" % Version.scalamock % Test,

    "com.roundeights" %% "hasher" % "1.2.0",

    "org.apache.poi" % "poi" % Version.apachePoi,
    "org.apache.poi" % "poi-ooxml" % Version.apachePoi,
    "org.apache.poi" % "poi-scratchpad" % Version.apachePoi,

    "com.jsuereth" %% "scala-arm" % "2.0",
    "io.github.cloudify" %% "spdf" % "1.4.0",

    "com.documents4j" % "documents4j-api" % "1.0.3",
    "com.documents4j" % "documents4j-local" % "1.0.3",
    "com.documents4j" % "documents4j-transformer-msoffice-word" % "1.0.3",
    "commons-io" % "commons-io" % "2.5",
    "com.google.guava" % "guava" % "18.0",
    "fr.opensagres.xdocreport" % "fr.opensagres.poi.xwpf.converter.pdf" % "2.0.1"
  )  ++ tests ++ guice ++ reflect

  val reflect = Seq(
    "org.scala-lang" % "scala-reflect" % Version.scala
  )
  val guice = Seq(
    "com.google.inject" % "guice" % Version.guice,
    "net.codingwell" %% "scala-guice" % Version.guice
  )

  val tests = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "com.typesafe.akka" %% "akka-testkit" % Version.akka % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % Test,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % Version.akkaPersistenceInmemoryVersion % Test,
    "commons-io" % "commons-io" % "2.5" % "test"
    //"biz.lobachev" %% "annette-core-test" % "3.1.0" % Test
  )
}
