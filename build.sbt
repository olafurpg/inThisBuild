def scala212 = "2.12.13"
def scala213 = "2.13.4"
def scala3 = "3.0.0-M3"
def all = List(scala212, scala213, scala3)
inThisBuild(List(
  scalaVersion := scala212,
  crossScalaVersions := all
))
crossScalaVersions := Nil

lazy val a = project
  .settings(
    scalaVersion := scala213,
    crossScalaVersions := List(scala212, scala213)
  )

lazy val b = project
  .settings(
    scalaVersion := scala213,
    crossScalaVersions := all
  )
  .dependsOn(a)
