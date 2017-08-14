import sbt._

object Dependencies {
  lazy val graphcore = "org.scala-graph" %% "graph-core" % "1.11.5"
  lazy val apachepoi = "org.apache.poi" % "poi" % "3.16"
  lazy val apacheooxml = "org.apache.poi" % "poi-ooxml" % "3.16"
  lazy val apacheooxmlschema = "org.apache.poi" % "poi-ooxml-schemas" % "3.16"
}
