import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val myproject = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(
    scalaVersion := "2.12.6",
    addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.0.0" cross CrossVersion.full)
  )
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))
lazy val myprojectJVM = myproject.jvm
lazy val myprojectJS = myproject.js
