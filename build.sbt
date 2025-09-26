ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.3"

lazy val root = (project in file("."))
  .settings(
    name := "zionomicon_book_practice"
  )

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.1.21"
)