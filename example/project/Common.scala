import sbt._
import Keys._

object Common {
  def version = "master-SNAPSHOT"
  def playVersion = "2.3.10"
  def scalaVersion =  "2.11.7"

  def resolvers = Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ,"Sonatype repository" at "https://oss.sonatype.org/content/repositories/releases/"
  )

  def dataDependencies = Seq(
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
    ,"org.apache.commons" % "commons-lang3" % "3.3.2"
    ,"org.reactivemongo" %% "reactivemongo" % "0.11.5"
    ,"org.scalaz" %% "scalaz-core" % "7.1.2"
    ,"joda-time" % "joda-time" % "2.8.1"
  )

  def appDependencies = dataDependencies
}
