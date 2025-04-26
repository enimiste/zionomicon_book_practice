ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "zionomicon_book_practice",
    idePackagePrefix := Some("edu.zionomicon")
  )

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.1.17"
)