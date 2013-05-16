import sbt._
import Keys._

object SbtProfilePlugin extends Plugin {

  case class SbtProfileSetting(profileName: String,
                               resourceDirs: Seq[File] = Nil,
                               sourceDirs: Seq[File] = Nil,
                               publishTo: Option[Resolver] = None,
                               overrideSettings: Seq[sbt.Project.Setting[_]] = Nil)

  object SbtProfileKeys {
    val buildProfile = SettingKey[String]("sbt-profile-active", "SBT Profile plugin's currently activated profile")
    val buildProfiles = SettingKey[Seq[String]]("sbt-profile-available", "SBT Profile plugin's all available profiles")
  }

  import SbtProfileKeys._


  lazy val profileSettings = Seq(
    buildProfile := "dev",
    buildProfiles := Seq("dev", "prod", "qa"),
    unmanagedResourceDirectories in Compile <<= (unmanagedResourceDirectories in Compile, baseDirectory, buildProfile) {
      (urs, bd, p) => {
        val resourceDir = bd / "src" / "main" / "profile" / p / "resources"
        if (resourceDir.exists) {
          urs ++ Seq(resourceDir)
        } else {
          urs
        }
      }
    }
  )
}
