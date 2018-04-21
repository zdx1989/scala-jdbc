organization := "com.github.zdx"

name := "scala-jdbc"

version := "0.1.1"

scalaVersion := "2.11.8"

lazy val shapelessV = "2.3.3"
lazy val scalatestV = "3.0.1"
lazy val h2V = "1.4.196"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % shapelessV,
  "org.scalatest" %% "scalatest" % scalatestV % Test,
  "com.h2database" % "h2" %  h2V % Test
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
