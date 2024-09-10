import com.typesafe.sbt.packager.docker.{Cmd, LayeredMapping}
import sbt.Keys._
import sbt._

import java.nio.file.Paths

ThisBuild / credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "raw-labs",
  sys.env.getOrElse("GITHUB_TOKEN", "")
)

val isRelease = sys.props.getOrElse("release", "false").toBoolean

lazy val commonSettings = Seq(
  homepage := Some(url("https://www.raw-labs.com/")),
  organization := "com.raw-labs",
  organizationName := "RAW Labs SA",
  organizationHomepage := Some(url("https://www.raw-labs.com/")),
  // Use cached resolution of dependencies
  // http://www.scala-sbt.org/0.13/docs/Cached-Resolution.html
  updateOptions := updateOptions.in(Global).value.withCachedResolution(true),
  resolvers ++= Seq(Resolver.mavenLocal),
  resolvers += "GHR snapi repo" at "https://maven.pkg.github.com/raw-labs/snapi",
  resolvers += "GHR protocol-das repo" at "https://maven.pkg.github.com/raw-labs/protocol-das",
  resolvers += "GHR das-sdk-scala repo" at "https://maven.pkg.github.com/raw-labs/das-sdk-scala",
  resolvers += "GHR das-server-scala repo" at "https://maven.pkg.github.com/raw-labs/das-server-scala",
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
  resolvers ++= Resolver.sonatypeOssRepos("releases")
)

lazy val buildSettings = Seq(
  scalaVersion := "2.12.18",
  isSnapshot := !isRelease,
  javacOptions ++= Seq(
    "-source",
    "21",
    "-target",
    "21"
  ),
  scalacOptions ++= Seq(
    "-feature",
    "-unchecked",
    // When compiling in encrypted drives in Linux, the max size of a name is reduced to around 140
    // https://unix.stackexchange.com/a/32834
    "-Xmax-classfile-name",
    "140",
    "-deprecation",
    "-Xlint:-stars-align,_",
    "-Ywarn-dead-code",
    "-Ywarn-macros:after", // Fix for false warning of unused implicit arguments in traits/interfaces.
    "-Ypatmat-exhaust-depth",
    "160"
  )
)

lazy val compileSettings = Seq(
  Compile / doc / sources := Seq.empty,
  Compile / packageDoc / mappings := Seq(),
  Compile / packageSrc / publishArtifact := true,
  Compile / packageDoc / publishArtifact := false,
  Compile / packageBin / packageOptions += Package.ManifestAttributes(
    "Automatic-Module-Name" -> name.value.replace('-', '.')
  ),
  // Ensure Java annotations get compiled first, so that they are accessible from Scala.
  compileOrder := CompileOrder.JavaThenScala
)

lazy val testSettings = Seq(
  // Ensuring tests are run in a forked JVM for isolation.
  Test / fork := true,
  // Disabling parallel execution of tests.
  //Test / parallelExecution := false,
  // Pass system properties starting with "raw." to the forked JVMs.
  Test / javaOptions ++= {
    import scala.collection.JavaConverters._
    val props = System.getProperties
    props
      .stringPropertyNames()
      .asScala
      .filter(_.startsWith("raw."))
      .map(key => s"-D$key=${props.getProperty(key)}")
      .toSeq
  },
  // Set up heap dump options for out-of-memory errors.
  Test / javaOptions ++= Seq(
    "-XX:+HeapDumpOnOutOfMemoryError",
    s"-XX:HeapDumpPath=${Paths.get(sys.env.getOrElse("SBT_FORK_OUTPUT_DIR", "target/test-results")).resolve("heap-dumps")}"
  ),
  Test / publishArtifact := true
)

val isCI = sys.env.getOrElse("CI", "false").toBoolean

lazy val publishSettings = Seq(
  versionScheme := Some("early-semver"),
  publish / skip := false,
  publishMavenStyle := true,
  publishTo := Some("GitHub raw-labs Apache Maven Packages" at "https://maven.pkg.github.com/raw-labs/das-salesforce"),
  publishConfiguration := publishConfiguration.value.withOverwrite(isCI)
)

lazy val strictBuildSettings = commonSettings ++ compileSettings ++ buildSettings ++ testSettings ++ Seq(
  scalacOptions ++= Seq(
    "-Xfatal-warnings"
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "das-salesforce",
    strictBuildSettings,
    publishSettings,
    libraryDependencies ++= Seq(
      "com.raw-labs" %% "das-sdk-scala" % "0.1.0" % "compile->compile;test->test",
      "com.frejo" % "force-rest-api" % "0.0.45",
      "joda-time" % "joda-time" % "2.12.7",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.12.7"
    )
  )

val amzn_jdk_version = "21.0.4.7-1"
val amzn_corretto_bin = s"java-21-amazon-corretto-jdk_${amzn_jdk_version}_amd64.deb"
val amzn_corretto_bin_dl_url = s"https://corretto.aws/downloads/resources/${amzn_jdk_version.replace('-', '.')}"

lazy val dockerSettings = strictBuildSettings ++ Seq(
  name := "das-salesforce-server",
  dockerBaseImage := s"--platform=amd64 debian:bookworm-slim",
  dockerLabels ++= Map(
    "vendor" -> "RAW Labs SA",
    "product" -> "das-salesforce-server",
    "image-type" -> "final"
  ),
  Docker / daemonUser := "raw",
  dockerExposedVolumes := Seq("/var/log/raw"),
  dockerExposedPorts := Seq(54322),
  dockerEnvVars := Map("PATH" -> s"${(Docker / defaultLinuxInstallLocation).value}/bin:$$PATH"),
  // We remove the automatic switch to USER 1001:0.
  // We we want to run as root to install the JDK, also later we will switch to a non-root user.
  // We will switch to a non-root user later in the Dockerfile.
  dockerCommands := dockerCommands.value.filterNot {
    case Cmd("USER", args @ _*) => args.contains("1001:0")
    case cmd => false
  },
  dockerCommands ++= Seq(
    Cmd(
      "RUN",
      s"""set -eux \\
      && apt-get update \\
      && apt-get install -y --no-install-recommends \\
        curl wget ca-certificates gnupg software-properties-common fontconfig java-common \\
      && wget $amzn_corretto_bin_dl_url/$amzn_corretto_bin \\
      && dpkg --install $amzn_corretto_bin \\
      && rm -f $amzn_corretto_bin \\
      && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \\
          wget gnupg software-properties-common"""
    ),
    Cmd(
      "USER",
      "raw"
    ),
    Cmd(
      "HEALTHCHECK",
      "--interval=3s --timeout=1s CMD curl --silent --head --fail http://localhost:54322/health || exit 1"
    )
  ),
  dockerEnvVars += "LANG" -> "C.UTF-8",
  dockerEnvVars += "JAVA_HOME" -> "/usr/lib/jvm/java-21-amazon-corretto",
  Compile / doc / sources := Seq.empty, // Do not generate scaladocs
  // Skip docs to speed up build
  Compile / packageDoc / mappings := Seq(),
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
  Compile / mainClass := Some("com.rawlabs.das.server.DASServerMain"),
  Docker / dockerAutoremoveMultiStageIntermediateImages := false,
  dockerAlias := dockerAlias.value.withTag(Option(version.value.replace("+", "-"))),
  dockerAliases := {
    val devRegistry = sys.env.getOrElse("DEV_REGISTRY", "ghcr.io/raw-labs/das-salesforce")
    val releaseRegistry = sys.env.get("RELEASE_DOCKER_REGISTRY")
    val baseAlias = dockerAlias.value.withRegistryHost(Some(devRegistry))

    releaseRegistry match {
      case Some(releaseReg) => Seq(
          baseAlias,
          dockerAlias.value.withRegistryHost(Some(releaseReg))
        )
      case None => Seq(baseAlias)
    }
  }
)

lazy val docker = (project in file("docker"))
  .dependsOn(
    root
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    buildSettings,
    dockerSettings,
    libraryDependencies += "com.raw-labs" %% "das-server-scala" % "0.1.0" % "compile->compile;test->test"
  )
