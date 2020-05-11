import mill._, scalalib._, scalafmt.ScalafmtModule

object bencode extends Module {
  def millSourcePath: os.Path = millOuterCtx.millSourcePath
  def ivyDeps = Agg(
    ivy"org.scodec::scodec-core:${Versions.scodec}".withDottyCompat(scalaVersion())
  )
  object test extends TestModule
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

