import sbt._
import Keys._

object Common {
  def version = "master-SNAPSHOT"
  def playVersion = "2.3.8"
  def scalaVersion =  "2.11.6"

  def resolvers = Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ,"Sonatype repository" at "https://oss.sonatype.org/content/repositories/releases/"
  ) 

  def dataDependencies = Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    ,"org.apache.commons" % "commons-lang3" % "3.3.2"
    ,"org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23"
    ,"org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"
    ,"com.typesafe.akka" %% "akka-actor" % "2.3.9"
    ,"org.scalaz" %% "scalaz-core" % "7.1.1"
    ,"joda-time" % "joda-time" % "2.7"
  )

  def appDependencies = dataDependencies
}
