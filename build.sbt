scalaVersion in ThisBuild := "2.13.5"
description in ThisBuild := "Tapir is a Chad's library for writing microservices in Scala"

val tapirVersion = "0.18.0-M7"

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %%% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %%% "tapir-json-circe" % tapirVersion,
      "io.circe" %%% "circe-generic" % "0.13.0"
      /**
       * Other available Tapir JSON codecs:
       * tapir-json-upickle
       * tapir-json-play
       * tapir-json-spray
       * tapir-json-tethys
       * tapir-jsoniter-scala
       * tapir-json-json4s
       */
    )
  )

lazy val server = project.in(file("server"))
  .dependsOn(shared.jvm)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
      "dev.zio" %% "zio-macros" % "1.0.6",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "ch.qos.logback" % "logback-core" % "1.2.3"
      /**
       * Other available server implementations
       * tapir-akka-http-server
       * tapir-play-server
       * tapir-http4s-server (Cats-effect based)
       * tapir-finatra-server (Twitter Futures :evil:)
       * tapir-vertx-server (Futures, Cats-effect, ZIO)
       *
       * Redoc and Swagger docs hosting available for majority of implementations
       */
    ),
    scalacOptions += "-Ymacro-annotations",
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full)
  )

lazy val app = project.in(file("app"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(shared.js)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "0.12.2",
      "com.softwaremill.sttp.tapir" %%% "tapir-sttp-client" % tapirVersion,
      "io.github.cquiroz" %%% "scala-java-time" % "2.2.0" // implementations of java.time classes for Scala.JS
    )
  )

