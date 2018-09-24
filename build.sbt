import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val myproject = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(
    scalaVersion := "2.12.6",
    addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.0.0" cross CrossVersion.full)
  )
  .jsSettings(
    npmDependencies in Compile += "snabbdom" -> "0.5.3",
    scalaJSUseMainModuleInitializer := true
  )
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))
lazy val myprojectJVM = myproject.jvm
lazy val myprojectJS = myproject.js
