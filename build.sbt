import SbtDASPlugin.autoImport.*

lazy val root = (project in file("."))
  .enablePlugins(SbtDASPlugin)
  .settings(
    repoNameSetting := "das-salesforce",
    libraryDependencies ++= Seq(
      // DAS
      "com.raw-labs" %% "das-server-scala" % "0.6.0" % "compile->compile;test->test",
      // Salesforce client
      "com.frejo" % "force-rest-api" % "0.0.45",
      // Jackson
      "joda-time" % "joda-time" % "2.12.7",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.18.2",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.18.2",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.18.2",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.18.2"))
