import sbt._
import Keys._

object MultiBuild extends Build {

  val baseSettings = Defaults.defaultSettings ++ Seq(
    version := "0.1",
    organization := "xyz.abc",
    sourceDirectories in Compile += new File("src/main/profile/dev")
  )

  lazy val modules = Project(id = "modules", base = file("."),
    settings = baseSettings ++ Seq(
      name := "multi-module"
    )
  ) aggregate(core, web)

  lazy val core = Project(id = "core", base = file("core"),
    settings = baseSettings ++ Seq(
      name := "core"
    )
  )

  lazy val web = Project(id = "web", base = file("web"),
    settings = baseSettings ++ Seq(
      name := "web"
    )
  ) dependsOn (core)

}