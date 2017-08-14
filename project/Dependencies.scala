import sbt._

object Dependencies {
  lazy val graphcore = "org.scala-graph" %% "graph-core" % "1.11.5"
  lazy val apachepoi = "org.apache.poi" % "poi" % "3.16"
  lazy val apacheooxml = "org.apache.poi" % "poi-ooxml" % "3.16"
  lazy val apacheooxmlschema = "org.apache.poi" % "poi-ooxml-schemas" % "3.16"
  lazy val breeze = "org.scalanlp" %% "breeze" % "0.13.2"
  lazy val breezenatives = "org.scalanlp" %% "breeze-natives" % "0.13.2"
  lazy val breezeviz = "org.scalanlp" %% "breeze-viz" % "0.13.2"
}
