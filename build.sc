import mill._, scalalib._

object ptv extends ScalaModule {
    def scalaVersion = "2.13.2"
    
    def ivyDeps = Agg(
        ivy"org.http4s::http4s-dsl:0.21.5",
        ivy"org.http4s::http4s-blaze-server:0.21.5",
        ivy"org.http4s::http4s-circe:0.21.5",
        ivy"io.circe::circe-generic:0.13.0",
        ivy"io.circe::circe-generic-extras:0.13.0",
        ivy"io.circe::circe-literal:0.13.0",
        ivy"com.lihaoyi::os-lib:0.7.0",
        ivy"ch.qos.logback:logback-classic:1.2.3",
        ivy"com.github.pureconfig::pureconfig:0.13.0"
    )

    def scalacOptions = Seq(
        "-Ymacro-annotations"
    )
}
