val scalaV = "3.3.1"
lazy val sparkCore = "org.apache.spark" %% "spark-core" % "3.5.0"
lazy val sparkSQL = "org.apache.spark" %% "spark-sql" % "3.5.0"
lazy val sparkMLlib = "org.apache.spark" %% "spark-mllib" % "3.5.0"
lazy val deltaCore = "io.delta" %% "delta-core" % "2.4.0"
lazy val deltaSpark = "io.delta" %% "delta-spark" % "3.0.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "sparky",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      sparkCore.cross(CrossVersion.for3Use2_13),
      sparkSQL.cross(CrossVersion.for3Use2_13),
      sparkMLlib.cross(CrossVersion.for3Use2_13),
      "io.github.vincenzobaz" %% "spark-scala3-encoders" % "0.2.4",
      "io.github.vincenzobaz" %% "spark-scala3-udf" % "0.2.4",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    ),
  )

run / javaOptions ++= Seq(
  "java.lang",
  "java.lang.invoke",
  "java.lang.reflect",
  "java.io",
  "java.net",
  "java.nio",
  "java.util",
  "java.util.concurrent",
  "java.util.concurrent.atomic",
  "sun.nio.ch",
  "sun.nio.cs",
  "sun.security.action",
  "sun.util.calendar"
).map(p => s"--add-opens=java.base/$p=ALL-UNNAMED")

run / fork := true
