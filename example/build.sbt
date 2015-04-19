name := """example"""

version := Common.version

scalaVersion := Common.scalaVersion

EclipseKeys.withSource := true

EclipseKeys.skipParents in ThisBuild := false

resolvers ++= Common.resolvers

libraryDependencies ++= Common.appDependencies

fork in run := true
