import mill._, scalalib._, publish._, scalafmt.ScalafmtModule

import $ivy.`com.lihaoyi::mill-contrib-bintray:$MILL_VERSION`
import mill.contrib.bintray.BintrayPublishModule

object bencode extends Module with BintrayPublishModule {
  def millSourcePath: os.Path = millOuterCtx.millSourcePath
  def ivyDeps = Agg(
    ivy"org.scodec::scodec-core:${Versions.scodec}".withDottyCompat(scalaVersion())
  )

  object test extends TestModule

  def bintrayOwner = "lavrov"
  def bintrayRepo = "maven"
  
  def pomSettings = PomSettings(
    description = "Bencode codec",
    organization = "com.github.styx-torrent",
    url = "https://github.com/styx-torrent/bencode",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("styx-torrent", "bencode"),
    developers = Seq(
      Developer("lavrov", "Vitaly Lavrov","https://github.com/lavrov")
    )
  )
  def publishVersion = "0.1.0"

}


trait Module extends SbtModule with ScalafmtModule {
  def scalaVersion = "0.24.0-RC1"

  def scalacOptions = List(
    "-language:implicitConversions"
  )

  trait TestModule extends Tests {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit:0.7.5"
    )
    def testFrameworks = Seq("munit.Framework")
  }
}

object Versions {
  val scodec = "1.11.4"
}

