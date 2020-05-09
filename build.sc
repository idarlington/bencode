import mill._, scalalib._, scalafmt.ScalafmtModule

object bencode extends Module {
  def ivyDeps = Agg(
    ivy"org.scodec::scodec-core:1.11.4".withDottyCompat(scalaVersion()), 
    ivy"org.typelevel::cats-core:${Versions.cats}".withDottyCompat(scalaVersion()),
  )
  object test extends TestModule
}


trait Module extends ScalaModule with ScalafmtModule {
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
  val cats = "2.2.0-M1"
  val `cats-effect` = "2.1.3"
  val monocle = "2.0.0"
  val logstage = "0.10.2"
  val `scodec-bits` = "1.1.14"
  val upickle = "1.0.0"
  val http4s = "0.21.1"
}

