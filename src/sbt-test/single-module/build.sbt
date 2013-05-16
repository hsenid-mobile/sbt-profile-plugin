import SbtProfilePlugin._
import SbtProfileKeys._

seq(baseProfileSettings :_*)

buildProfile := "prod"

buildProfiles := Seq("dev", "prod")

organization := "org.xyz.Main"

name := "xyz-app"

version := "1.0"
