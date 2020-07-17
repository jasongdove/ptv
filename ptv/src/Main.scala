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

  override def run(args: List[String]): IO[ExitCode] = resources.use(_ => IO.never)

  def resources =
    for {
      blocker <- Blocker[IO]
      config = ConfigSource.default.loadOrThrow[PtvConfig]
      loggerApp = RequestLogger(logHeaders = false, logBody = false, FunctionK.id[IO])(app(blocker, config))
      server <- BlazeServerBuilder[IO](global).withHttpApp(loggerApp).bindHttp(config.port, config.host).resource
    } yield ExitCode.Success

}
