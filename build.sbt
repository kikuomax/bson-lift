organization := "com.github.kikuomax"

name := "bson-lift"

version := "0.1.0"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.10.6", "2.11.7")

scalacOptions := Seq("-feature", "-deprecation", "-unchecked", "-encoding", "utf-8")

scalacOptions in Test += "-Yrangepos"

scalacOptions in doc := Seq("-deprecation", "-encoding", "utf-8")

libraryDependencies ++= Seq(
  "org.mongodb" % "mongodb-driver" % "3.2.1",
  "org.specs2" %% "specs2-core" % "3.7" % "test"
)
