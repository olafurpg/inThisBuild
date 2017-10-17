lazy val a = project.settings(
  scalaVersion := "2.12.3",
  scalacOptions ++= List(
    "-Yrangepos"
    // "-Xplugin-require:macro-paradise-plugin"
    // "-Xplugin-require:semanticdb"
  ),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "2.0.1" cross CrossVersion.full),
  libraryDependencies += "io.circe" %% "circe-generic" % "0.8.0"
)
