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
    ,"org.reactivemongo" %% "reactivemongo" % "0.11.7"
    ,"org.scala-lang.modules" %% "scala-xml" % "1.0.5"
    ,"org.scalaz" %% "scalaz-core" % "7.1.3"
    ,"joda-time" % "joda-time" % "2.8.2"
    ,"com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
  )

  def appDependencies = dataDependencies
}
