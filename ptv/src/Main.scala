import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.circe._
import io.circe.syntax._
import fs2._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.middleware.RequestLogger
import scala.concurrent.ExecutionContext.global
import HDHR._
import org.http4s.parser.ContentTypeHeader
import org.http4s.MediaType
import org.http4s.Header
import cats.arrow.FunctionK
import org.http4s.StaticFile
import java.io.File
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Main extends IOApp {
  def getDevice(tunerCount: Int, host: String) = Device(
    "PseudoTVScala",
    "PseudoTV - Silicondust",
    s"https://$host/ptv",
    "HDTC-2US",
    "hdhomeruntc_atsc",
    "20190621",
    tunerCount,
    "PseudoTVScala",
    "",
    host,
    s"http://$host/lineup.json"
  )

  def getChannels(host: String) = List(
    Channel(1, "Test Channel 1", s"http://$host/video?channel=1")
  )

  val lineupStatus = LineupStatus(0, 1, "Cable", List("Cable"))

  def app(blocker: Blocker, device: Device, channels: List[Channel], testFile: String) =
    HttpRoutes
      .of[IO] {
        case GET -> Root / "discover.json" =>
          Ok(device.asJson)
        case GET -> Root / "lineup_status.json" =>
          Ok(lineupStatus.asJson)
        case GET -> Root / "lineup.json" =>
          Ok(channels.asJson)
        case GET -> Root / "auto" / channel => {
          val ffmpeg = os
            .proc(
              "ffmpeg",
              "-threads",
              "4",
              "-i",
              testFile,
              "-f",
              "mpegts",
              "-c:v",
              "mpeg2video",
              "-qscale:v",
              "2",
              "-c:a",
              "ac3",
              "pipe:1"
            )
            .spawn()

          val stream = io.readInputStream(
            IO(ffmpeg.stdout.wrapped),
            65535,
            blocker,
            true
          )

          Ok(stream, Header("Content-Type", "video/mpeg"))
        }
      }
      .orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    val config = ConfigSource.default.loadOrThrow[ServiceConf]

    val blocker = Blocker[IO]

    val device = getDevice(config.tunerCount, config.host)
    val channels = getChannels(config.host)

    blocker.use(b =>
      BlazeServerBuilder[IO](global)
        .bindHttp(config.port, config.host)
        .withHttpApp(
          RequestLogger(false, false, FunctionK.id[IO])(app(b, device, channels, config.testFile))
        )
        .serve
        .compile
        .drain
    ).as(ExitCode.Success)
  }
}
