import sbt._
import Keys._

object Common {
  def version = "master-SNAPSHOT"
  def playVersion = "2.3.9"
  def scalaVersion =  "2.11.6"

  def resolvers = Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ,"Sonatype repository" at "https://oss.sonatype.org/content/repositories/releases/"
  ) 

  def dataDependencies = Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    ,"org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23"
    ,"org.scala-lang.modules" %% "scala-xml" % "1.0.3"
    ,"org.scalaz" %% "scalaz-core" % "7.1.1"
    ,"joda-time" % "joda-time" % "2.7"
  )

  def appDependencies = dataDependencies
}
