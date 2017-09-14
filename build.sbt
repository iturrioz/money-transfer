name := """money-transfer"""
organization := "net.iturrioz"

version := "0.2-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .enablePlugins(PlayScala)
  .settings(coverageExcludedPackages := "<empty>;Reverse.*;.*Routes.*")

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "net.iturrioz.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "net.iturrioz.binders._"
