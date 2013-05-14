import sbt._
import Keys._

object SbtProfilePlugin extends Plugin {
  Compile
  case class SbtProfileSetting(profileName: String,
                               resourceDirs: Seq[File] = Nil,
                               sourceDirs: Seq[File] = Nil,
                               publishTo: Option[Resolver] = None,
                               overrideSettings: Seq[sbt.Project.Setting[_]] = Nil)

  object SbtProfileKeys {
    val buildProfile = SettingKey[String]("profile", "profile-name")
    val buildProfiles = SettingKey[Seq[String]]("profiles", "available profile list")
    val profileSettingList = SettingKey[Seq[SbtProfileSetting]]("profile-settings", "Profile settings foreach profile")

    val showProfiles = TaskKey[Unit]("show-profiles", "show profile list")
    val additionalResourceDir = TaskKey[Seq[File]]("profile-resource-directories", "Additional resource directories for profile")
    val additionalSrcDir = TaskKey[Seq[File]]("profile-source-directories", "Additional source directories for profile")
    val overridePublishTo = TaskKey[Option[Resolver]]("override-publish-to", "Publish target for profile")
    val overrideSettings = TaskKey[Seq[sbt.Project.Setting[_]]]("override-settings", "override settings for profile")
  }

  import SbtProfileKeys._

  override lazy val settings = Seq(
    commands ++= Seq(swichProfileCommand)
  )

  var firstCall_? = true

  def runProject(profileName: String) = (state: State) => {
    if (firstCall_?) {
      firstCall_? = false
      updateProperties(state, profileName)
    } else state
  }

  lazy val profileSettings = Seq(
    buildProfile := "development",
    buildProfiles := Seq("development", "test", "production"),
    showProfilesTask,
    additionalResourceDirTask,
    additionalSrcDirTask,
    overrideSettingsTask,
    overridePublishToTask,
    profileSettingList := Seq(),
    onLoad in Global <<= (buildProfile).apply( p => {
      runProject(p)
    })
  )


  val showProfilesTask = showProfiles <<= (buildProfiles) map (profiles => {
    profiles.foreach(println(_))
  })

  val additionalResourceDirTask = SbtProfileKeys.additionalResourceDir <<= (profileSettingList, buildProfile, baseDirectory) map (
    (pSettings, p, baseDir) => {
      LogManager.defaultScreen.error("************* current base dir " + baseDir)
      pSettings.find(_.profileName == p).map(_.resourceDirs) getOrElse Seq(baseDir / "src" / "main" / "profile" / p)
    })

  val additionalSrcDirTask = additionalSrcDir <<= (profileSettingList, buildProfile) map ((pSettings, p) => {
    //nothing
    pSettings.find(_.profileName == p).map(_.sourceDirs).getOrElse(Seq())
  })

  val overrideSettingsTask = overrideSettings <<= (profileSettingList, buildProfile) map ((pSettings, p) => {
    //nothing
    pSettings.find(_.profileName == p).map(_.overrideSettings).getOrElse(Nil)
  })

  val overridePublishToTask = overridePublishTo <<= (profileSettingList, buildProfile) map (
    (pSettings, p) => {
      pSettings.find(_.profileName == p).map(_.publishTo) getOrElse None
    }
    )


  lazy val swichProfileCommand = Command.single("set-profile") {
    updateProperties
  }

  def updateProperties(state: State, profileName: String) = {

    val extracted = Project.extract(state)

    val profiles = extracted.get(buildProfiles)

    if (!profiles.contains(profileName)) {
      LogManager.defaultScreen.error("Unknown profile '%s'!".format(profileName))
      state.fail
    } else {
      LogManager.defaultScreen.debug("Set profile to '%s'.".format(profileName))

      val baseDir = extracted.get(baseDirectory)
      val publishTarget = extracted.getOpt(publishTo).getOrElse(None)
      val (s1, resourceDirs) = extracted.runTask(SbtProfileKeys.additionalResourceDir, state)
      val unRes = extracted.getOpt(unmanagedResourceDirectories).getOrElse {
        Seq(baseDir / "src" / "main" / "resources")
      }
      val (s2, sourceDirs) = extracted.runTask(SbtProfileKeys.additionalSrcDir, s1)
      val unSrc = extracted.getOpt(unmanagedSourceDirectories).getOrElse {
        Seq(baseDir / "src" / "main" / "scala")
      }
      val (s3, oSettings) = extracted.runTask(SbtProfileKeys.overrideSettings, s2)

      val (s4, oPublishTo) = extracted.runTask(overridePublishTo, s3)

      println(overrideSettings)

      extracted.append(Seq(
        buildProfile := profileName,
        unmanagedResourceDirectories in Compile := (unRes ++ resourceDirs),
        unmanagedSourceDirectories in Compile := (unSrc ++ sourceDirs),
        publishTo := oPublishTo orElse publishTarget
      ) ++ oSettings, s4)
    }
  }

}