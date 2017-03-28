lazy val myproject = crossProject.settings(
  scalaVersion := "2.12.1",
  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.1" % Test
)
lazy val myprojectJVM = myproject.jvm
lazy val myprojectJS = myproject.js
