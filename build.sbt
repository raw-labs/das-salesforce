import sbt.Keys._
import sbt._

import com.typesafe.sbt.packager.docker.LayeredMapping

ThisBuild / credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "raw-labs",
  sys.env.getOrElse("GITHUB_TOKEN", ""))

// Extract organization username for GitHub
lazy val orgUsername = "raw-labs"
lazy val repoName = "das-salesforce"
lazy val gitHubRepo = s"$orgUsername/$repoName"
lazy val gitHubUrl = s"https://github.com/$gitHubRepo"
lazy val ghcrRegistry = s"ghcr.io/$gitHubRepo"
lazy val mavenRegistry = s"https://maven.pkg.github.com/$orgUsername/$repoName"

lazy val commonSettings = Seq(
  homepage := Some(url("https://www.raw-labs.com/")),
  organization := "com.raw-labs",
  organizationName := "RAW Labs SA",
  organizationHomepage := Some(url("https://www.raw-labs.com/")),
  // Use cached resolution of dependencies
  // http://www.scala-sbt.org/0.13/docs/Cached-Resolution.html
  updateOptions := updateOptions.in(Global).value.withCachedResolution(true),
  // Add local Maven repository to resolvers
  resolvers ++= Seq(Resolver.mavenLocal),
  // Add RAW Labs Package Registry to resolvers
  resolvers += "RAW Labs GitHub Packages" at "https://maven.pkg.github.com/raw-labs/_")

lazy val buildSettings = Seq(scalaVersion := "2.13.15")

lazy val compileSettings = Seq(
  Compile / doc / sources := Seq.empty,
  Compile / packageDoc / mappings := Seq(),
  Compile / packageSrc / publishArtifact := true,
  Compile / packageDoc / publishArtifact := false,
  Compile / packageBin / packageOptions += Package.ManifestAttributes(
    "Automatic-Module-Name" -> name.value.replace('-', '.')),
  // Ensure Java annotations get compiled first, so that they are accessible from Scala.
  compileOrder := CompileOrder.JavaThenScala,
  // Ensure we fork new JVM for run, so we can set JVM flags.
  Compile / run / fork := true,
  Compile / mainClass := Some("com.rawlabs.das.server.DASServer"))

lazy val testSettings = Seq(
  // Ensuring tests are run in a forked JVM for isolation.
  Test / fork := true,
  // Required for publishing test artifacts.
  Test / publishArtifact := true)

val isCI = sys.env.getOrElse("CI", "false").toBoolean

lazy val publishSettings = Seq(
  versionScheme := Some("early-semver"),
  publish / skip := false,
  publishMavenStyle := true,
  publishTo := Some("GitHub raw-labs Apache Maven Packages" at mavenRegistry),
  publishConfiguration := publishConfiguration.value.withOverwrite(isCI))

lazy val strictBuildSettings =
  commonSettings ++ compileSettings ++ buildSettings ++ testSettings ++ Seq(scalacOptions ++= Seq("-Xfatal-warnings"))

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    name := repoName,
    strictBuildSettings,
    publishSettings,
    libraryDependencies ++= Seq(
      // DAS
      "com.raw-labs" %% "das-server-scala" % "0.4.1" % "compile->compile;test->test",
      "com.raw-labs" %% "protocol-das" % "1.0.0" % "compile->compile;test->test",
      // Salesforce client
      "com.frejo" % "force-rest-api" % "0.0.45",
      // Jackson
      "joda-time" % "joda-time" % "2.12.7",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.18.2",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.18.2",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.18.2",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.18.2"),
    dockerSettings)

lazy val dockerSettings = Seq(
  Docker / packageName := s"$repoName-server",
  dockerBaseImage := "eclipse-temurin:21-jre",
  dockerLabels ++= Map(
    "vendor" -> "RAW Labs SA",
    "product" -> s"$repoName-server",
    "image-type" -> "final",
    "org.opencontainers.image.source" -> s"https://github.com/raw-labs/$repoName"),
  Docker / daemonUser := "raw",
  Docker / daemonUserUid := Some("1001"),
  Docker / daemonGroup := "raw",
  Docker / daemonGroupGid := Some("1001"),
  dockerExposedVolumes := Seq("/var/log/raw"),
  dockerExposedPorts := Seq(50051),
  dockerEnvVars := Map("PATH" -> s"${(Docker / defaultLinuxInstallLocation).value}/bin:$$PATH"),
  dockerEnvVars += "LANG" -> "C.UTF-8",
  updateOptions := updateOptions.value.withLatestSnapshots(true),
  Linux / linuxPackageMappings += packageTemplateMapping(s"/var/lib/${packageName.value}")(),
  bashScriptDefines := {
    val ClasspathPattern = "declare -r app_classpath=\"(.*)\"\n".r
    bashScriptDefines.value.map {
      case ClasspathPattern(classpath) => s"""
        |declare -r app_classpath="$${app_home}/../conf:$classpath"
        |""".stripMargin
      case _ @entry => entry
    }
  },
  Docker / dockerLayerMappings := (Docker / dockerLayerMappings).value.map {
    case lm @ LayeredMapping(Some(1), file, path) => {
      val fileName = java.nio.file.Paths.get(path).getFileName.toString
      if (!fileName.endsWith(".jar")) {
        // If it is not a jar, put it on the top layer. Configuration files and other small files.
        LayeredMapping(Some(2), file, path)
      } else if (fileName.startsWith("com.raw-labs") && fileName.endsWith(".jar")) {
        // If it is one of our jars, also top layer. These will change often.
        LayeredMapping(Some(2), file, path)
      } else {
        // Otherwise it is a 3rd party library, which only changes when we change dependencies, so leave it in layer 1
        lm
      }
    }
    case lm @ _ => lm
  },
  Docker / version := {
    val ver = version.value
    // Docker tags have their own restrictions - only allow [a-zA-Z0-9_.-]
    // Replace + with - and ensure no invalid characters
    ver.replaceAll("[+]", "-").replaceAll("[^\\w.-]", "-")
  },
  dockerAlias := {
    val devRegistry = sys.env.getOrElse("DEV_REGISTRY", ghcrRegistry)
    dockerAlias.value.withRegistryHost(Some(devRegistry))
  },
  dockerAliases := {
    val devRegistry = sys.env.getOrElse("DEV_REGISTRY", ghcrRegistry)
    val releaseRegistry = sys.env.get("RELEASE_DOCKER_REGISTRY")
    val baseAlias = dockerAlias.value.withRegistryHost(Some(devRegistry))

    releaseRegistry match {
      case Some(releaseReg) => Seq(baseAlias, dockerAlias.value.withRegistryHost(Some(releaseReg)))
      case None             => Seq(baseAlias)
    }
  })

lazy val printDockerImageName = taskKey[Unit]("Prints the full Docker image name that will be produced")

printDockerImageName := {
  // Get the main Docker alias (the first one in the sequence)
  val alias = (Docker / dockerAliases).value.head
  // The toString method already returns the full image name with registry and tag
  println(s"DOCKER_IMAGE=${alias}")
}
