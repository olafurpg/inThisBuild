scalaVersion := "2.12.4"
scalacOptions += "-Yrangepos"
addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "2.1.5" cross CrossVersion.full)
