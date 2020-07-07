import cats.arrow.FunctionK
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.middleware.RequestLogger
import pureconfig.ConfigSource

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  def app(
    blocker: Blocker,
    config: PtvConfig
  ): HttpApp[IO] = (HDHR(config, blocker) <+> XMLTV(blocker)).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    val config = ConfigSource.default.loadOrThrow[PtvConfig]

    Blocker[IO]
      .use(blocker =>
        BlazeServerBuilder[IO](global)
          .bindHttp(config.port, config.host)
          .withHttpApp(
            RequestLogger(logHeaders = false, logBody = false, FunctionK.id[IO])(
              app(blocker, config)
            )
          )
          .serve
          .compile
          .drain
      )
      .as(ExitCode.Success)
  }
}
