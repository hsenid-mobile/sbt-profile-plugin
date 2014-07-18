import sbt._
import Keys._
import SbtProfilePlugin._
import SbtProfilePlugin.SbtProfileKeys._
import com.github.siasia.WebPlugin._
import com.github.siasia.PluginKeys._

object MultiBuild extends Build {
  val tomcatVersion = "7.0.34"

  lazy val baseProfileSettings = Defaults.defaultSettings ++ profileSettings ++ Seq(
    buildProfile := "prod",
    buildProfiles := Seq("dev", "prod", "qa"),
    version := "0.1",
    organization := "xyz.abc"
  )

  lazy val modules = Project(id = "multi-module", base = file("."),
    settings = baseProfileSettings ++ Seq(
      name := "multi-module"
    )
  ) aggregate(core, web)


  lazy val core = Project(id = "core", base = file("core"),
    settings = baseProfileSettings ++ Seq(
      name := "core"
    )
  )

  lazy val web = Project(id = "web", base = file("web"),
    settings = baseProfileSettings ++ webSettings ++ Seq(
      name := "web",
      libraryDependencies ++= Seq(
        "org.apache.tomcat.embed" % "tomcat-embed-core" % tomcatVersion % "container",
        "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % tomcatVersion % "container",
        "org.apache.tomcat.embed" % "tomcat-embed-jasper" % tomcatVersion % "container",
        "org.apache.tomcat" % "tomcat-jasper" % tomcatVersion % "container",
        "org.apache.tomcat" % "tomcat-jasper-el" % tomcatVersion % "container",
        "org.apache.tomcat" % "tomcat-jsp-api" % tomcatVersion % "container",
        "javax.servlet" % "servlet-api" % "2.5" % "provided"
      ),
      webappResources in Compile <<= (webappResources in Compile, baseDirectory, buildProfile) {
        (wrs, bd, p) => {
          val resourceDir = bd / "src" / "main" / "profile" / p / "webapp"
          if (resourceDir.exists) {
            val allRs = wrs ++ Seq(resourceDir)
            allRs.reverse //is a hack to prefer profile webapp, than default webapp
          } else {
            wrs
          }
        }
      }

    )
  ) dependsOn (core)

}