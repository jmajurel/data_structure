import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.myself",
      scalaVersion := "2.12.2",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "graphpratice",
    libraryDependencies ++= Seq(graphcore, apachepoi, apacheooxml, apacheooxmlschema)   
  )
