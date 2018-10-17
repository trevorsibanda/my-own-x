name := """pastebin"""
organization := "zw.co.trevor"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

val elastic4sVersion = "6.3.7"

//slick
libraryDependencies ++= Seq(
  "javax.xml.bind" % "jaxb-api" % "2.3.0",
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
  "mysql" % "mysql-connector-java" % "5.1.32",
  "com.h2database" % "h2" % "1.4.181",
  "com.newmotion" %% "akka-rabbitmq" % "5.0.0",
  "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,
  // for the tcp client
  //"com.sksamuel.elastic4s" %% "elastic4s-tcp" % elastic4sVersion,

  // for the http client
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,

  // if you want to use reactive streams
  //"com.sksamuel.elastic4s" %% "elastic4s-streams" % elastic4sVersion,

  // a json library
  "com.sksamuel.elastic4s" %% "elastic4s-jackson" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-play-json" % elastic4sVersion,
  //"com.sksamuel.elastic4s" %% "elastic4s-circe" % elastic4sVersion,
  //"com.sksamuel.elastic4s" %% "elastic4s-json4s" % elastic4sVersion,

  // testing
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test",
  "com.sksamuel.elastic4s" %% "elastic4s-embedded" % elastic4sVersion % "test",

  "net.debasishg" %% "redisclient" % "3.8",
   //"io.monix" %% "shade" % "1.10.0"
)


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "zw.co.trevor.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "zw.co.trevor.binders._"
