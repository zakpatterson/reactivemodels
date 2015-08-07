import sbt._
import Keys._

object Common {
  def version = "master-SNAPSHOT"
  def playVersion = "2.3.9"
  def scalaVersion =  "2.11.7"

  def resolvers = Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ,"Sonatype repository" at "https://oss.sonatype.org/content/repositories/releases/"
  )

  def dataDependencies = Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    ,"org.reactivemongo" %% "reactivemongo" % "0.11.5"
    ,"org.scala-lang.modules" %% "scala-xml" % "1.0.3"
    ,"org.scalaz" %% "scalaz-core" % "7.1.1"
    ,"joda-time" % "joda-time" % "2.7"
    ,"com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
  )

  def appDependencies = dataDependencies
}
