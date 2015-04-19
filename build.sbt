name := """reactivemodels"""

version := Common.version

scalaVersion := Common.scalaVersion

EclipseKeys.withSource := true

EclipseKeys.skipParents in ThisBuild := false

resolvers ++= Common.resolvers

libraryDependencies ++= Common.appDependencies

fork in run := true

lazy val example = (project in file("example/")).enablePlugins(PlayScala).dependsOn(reactivemodels).aggregate(reactivemodels)

lazy val reactivemodels = (project in file(".")).enablePlugins(PlayScala)
