val imcVersion = "3.1.7-SNAPSHOT"
val coreVersion = "4.0.28-SNAPSHOT"

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

  mainClass in Compile := Some("annette.core.server.AnnetteApplication"),

  fork in run := false
)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(name := "sphera")
  .aggregate(annetteCore, annetteCoreTest, annetteSphera , annetteFrontendSphera)

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
    commonSettings
  )

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
  //.enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    name := "annette-sphera",
    version := imcVersion,
    //buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, buildInfoBuildNumber),
   // buildInfoOptions += BuildInfoOption.BuildTime,
   // buildInfoPackage := "annette.sphera",

    libraryDependencies ++= Dependencies.imc,
    unmanagedClasspath in Test += (resourceDirectory in Compile).value,

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),

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
    packageName in Universal := "annette-sphera-server"
  ).dependsOn(annetteSphera, annetteFrontendSphera)

mainClass in Compile := Some("annette.core.server.AnnetteApplication")
