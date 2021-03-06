import Dependencies._

val imcVersion = "3.1.7-SNAPSHOT"
val coreVersion = "4.0.28-SNAPSHOT"

// the library is available in Bintray's JCenter
resolvers += Resolver.jcenterRepo
resolvers ++= Seq(
  Resolver.bintrayRepo("naftoligug", "maven"),
  Resolver.sonatypeRepo("snapshots"))

lazy val commonSettings = Seq(


 // resolvers += "AnikLab private repository" at "http://dev.aniklab.com:8081/repository/maven-public/",
//  publishTo := {
//    val nexus = "http://dev.aniklab.com:8081/"
//    if (isSnapshot.value)
//      Some("AnikLab private snapshots" at nexus + "repository/maven-snapshots/")
//    else
//      Some("AnikLab private releases" at nexus + "repository/maven-releases/")
//  },
  // disable publishing the main sources jar
  publishArtifact in(Compile, packageSrc) := false,
  // the library is available in Bintray's JCenter
  resolvers += Resolver.jcenterRepo,
  organization := "com.aniklab",
  scalaVersion := Dependencies.Version.scala,
  logLevel := Level.Warn,
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-deprecation", // warning and location for usages of deprecated APIs
    "-feature", // warning and location for usages of features that should be imported explicitly
    "-unchecked", // additional warnings where generated code depends on assumptions
    "-language:postfixOps",
    "-language:implicitConversions",
    //"-Xlint", // recommended additional warnings
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
    //"-Ywarn-value-discard", // Warn when non-Unit expression results are unused
    "-Ywarn-inaccessible",
    "-Ywarn-dead-code"
  ),
  updateOptions := updateOptions.value.withLatestSnapshots(false),

  fork in run := false
)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(name := "sphera")
  .aggregate(annetteCore, annetteCoreTest, annetteSphera , annetteFrontendSphera)
  //.dependsOn(generatedCode)

lazy val annetteCore = Project(
  id = "annette-core",
  base = file("annette-core")
)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    name := "annette-core",
    version := coreVersion,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, buildInfoBuildNumber),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoPackage := "annette.core",
    libraryDependencies ++= Dependencies.core,

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),

    PB.deleteTargetDirectory := false,

    sourceDirectories in (Compile, TwirlKeys.compileTemplates) +=
      (scalaSource in Compile).value / "annette" / "core" / "notification" / "templates",
    commonSettings
  ).enablePlugins(SbtTwirl)

lazy val annetteCoreTest = Project(
  id = "annette-core-test",
  base = file("annette-core-test")
)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    name := "annette-core-test",
    version := coreVersion,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, buildInfoBuildNumber),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoPackage := "annette.core.test",
    libraryDependencies ++= Dependencies.core ++ Dependencies.testInCompile,
    commonSettings
  )
  .dependsOn(annetteCore)

lazy val annetteSphera = Project(
  id = "annette-sphera",
  base = file("annette-sphera")
)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    name := "annette-sphera",
    version := imcVersion,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, buildInfoBuildNumber),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoPackage := "annette.sphera",

    libraryDependencies ++= Dependencies.imc,
    unmanagedClasspath in Test += (resourceDirectory in Compile).value,

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),

    PB.deleteTargetDirectory := false,

    commonSettings
  ).dependsOn(annetteCore)

lazy val annetteFrontendSphera = Project(
  id = "annette-frontend-sphera",
  base = file("annette-frontend-sphera")
)
  .settings(
    commonSettings,
    name := "annette-frontend-sphera",
    version := imcVersion
  )

lazy val annetteSpheraServer = Project(
  id = "annette-sphera-server",
  base = file("annette-sphera-server")
)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    commonSettings,
    name := "annette-sphera-server",
    version := imcVersion,
    packageName in Universal := "annette-sphera-server",
    baseDirectory in reStart := (baseDirectory in root).value,
    mainClass in Compile := Some("annette.core.AnnetteApplication")
  ).dependsOn(annetteSphera, annetteFrontendSphera)

mainClass in Compile := Some("annette.core.AnnetteApplication")
mainClass in reStart := Some("annette.core.AnnetteApplication")


addCommandAlias("mgm", "migration_manager/run")

addCommandAlias("mg", "migrations/run")

lazy val slickVersion = "3.2.1"

lazy val forkliftVersion = "0.3.0"

lazy val loggingDependencies = List(
  "org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
)

lazy val slickDependencies = List(
  "com.typesafe.slick" %% "slick" % slickVersion
)

lazy val dbDependencies = List(
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "org.postgresql" % "postgresql" % "42.2.5"
)

lazy val forkliftDependencies = List(
  "com.liyaos" %% "scala-forklift-slick" % forkliftVersion
  ,"io.github.nafg" %% "slick-migration-api" % "0.4.1"
)

//lazy val appDependencies = dbDependencies ++ loggingDependencies

lazy val migrationsDependencies =
  dbDependencies ++ forkliftDependencies ++ loggingDependencies

lazy val migrationManagerDependencies = dbDependencies ++ forkliftDependencies

//lazy val app = Project("app",
//  file("app")).dependsOn(generatedCode).settings(
//  commonSettings:_*).settings {
//  libraryDependencies ++= appDependencies
//}

lazy val migrationManager = Project("migration_manager",
  file("migration_manager")).settings(
  commonSettings:_*).settings {
  libraryDependencies ++= migrationManagerDependencies
}

lazy val migrations = Project("migrations",
  file("migrations")).dependsOn(
  generatedCode, migrationManager).settings(
  commonSettings:_*).settings {
  libraryDependencies ++= migrationsDependencies
}

lazy val tools = Project("git-tools",
  file("tools/git")).settings(commonSettings:_*).settings {
  libraryDependencies ++= forkliftDependencies ++ List(
    "com.liyaos" %% "scala-forklift-git-tools" % forkliftVersion,
    "com.typesafe" % "config" % "1.3.0",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r"
  )
}

lazy val generatedCode = Project("generate_code",
  file("generated_code")).settings(commonSettings:_*).settings {
  libraryDependencies ++= slickDependencies
}