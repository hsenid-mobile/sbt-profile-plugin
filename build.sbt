sbtPlugin := true

name := "sbt-profile-plugin"

organization := "hms.sbt.plugin"

version := "0.1.4"

libraryDependencies += "com.earldouglas" %% "xsbt-web-plugin" % "0.3" % "provided"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials-release")

resolvers += "sonatype releases" at "https://oss.sonatype.org/content/groups/public"

publishTo <<= (version) { version: String =>
  val repo = "http://192.168.0.7:8080/archiva/repository/"
  if (version.trim.endsWith("SNAPSHOT"))
    Some("Repository Archiva Managed snapshots Repository" at repo + "snapshots/")
  else
    Some("Repository Archiva Managed internal Repository" at repo + "internal/")
}
