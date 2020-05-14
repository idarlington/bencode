lazy val bencode = project
  .in(file("."))
  .settings(
    organization := "com.github.styx-torrent",
    version := "0.1.0",
    scalaVersion := "0.24.0-RC1",
    libraryDependencies ++= Seq(
      ("org.scodec" %% "scodec-core" % Versions.scodec).withDottyCompat(scalaVersion.value),
      "org.scalameta" %% "munit" % "0.7.5" % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    bintrayOrganization := Some("lavrov"),
    bintrayRepository := "maven",
    licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
  )

val Versions = new {
  val scodec = "1.11.4"
}