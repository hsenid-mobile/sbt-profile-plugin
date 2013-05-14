import ScriptedPlugin._

seq(scriptedSettings: _*)

sbtPlugin := true

name := "sbt-profile-plugin"

organization := "hms.sbt.plugin"

version := "0.1"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials-release")


publishTo <<= (version) { version: String =>
  val repo = "http://192.168.0.7:8080/archiva/repository/"
  if (version.trim.endsWith("SNAPSHOT"))
    Some("Repository Archiva Managed snapshots Repository" at repo + "snapshots/")
  else
    Some("Repository Archiva Managed internal Repository" at repo + "internal/")
}
