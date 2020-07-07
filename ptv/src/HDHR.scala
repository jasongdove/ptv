import cats.effect.{Blocker, ContextShift, IO}
import io.circe.generic.extras._
import io.circe.syntax._
import fs2._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{Header, HttpRoutes}

object HDHR {
  implicit val config: Configuration = Configuration.default.copy(
    transformMemberNames = x => x.capitalize
  )

  @ConfiguredJsonCodec case class Device(
    friendlyName: String,
    manufacturer: String,
    manufacturerURL: String,
    modelNumber: String,
    firmwareName: String,
    firmwareVersion: String,
    tunerCount: Int,
    deviceID: String,
    deviceAuth: String,
    baseURL: String,
    lineupURL: String
  )

  @ConfiguredJsonCodec case class LineupStatus(
    scanInProgress: Int,
    scanPossible: Int,
    source: String,
    sourceList: List[String]
  )

  @ConfiguredJsonCodec case class Channel(
    guideNumber: Int,
    guideName: String,
    URL: String
  )

  def streamForChannel(config: PtvConfig, blocker: Blocker, channel: String)(
    implicit cs: ContextShift[IO]
  ): Stream[IO, Byte] = {
    val ffmpeg = os
      .proc(
        "ffmpeg",
        "-threads",
        "4",
        "-i",
        config.testFile,
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

    io.readInputStream(
      IO(ffmpeg.stdout.wrapped),
      chunkSize = 65535,
      blocker,
      closeAfterUse = true
    )
  }

  def apply(config: PtvConfig, blocker: Blocker)(
    implicit cs: ContextShift[IO]
  ): HttpRoutes[IO] = {
    val device = Device(
      "PseudoTVScala",
      "PseudoTV - Silicondust",
      "https://github.com/jasongdove/ptv",
      "HDTC-2US",
      "hdhomeruntc_atsc",
      "20190621",
      config.tunerCount,
      "PseudoTVScala",
      "",
      config.host,
      s"http://${config.host}:${config.port}/lineup.json"
    )

    val lineupStatus = LineupStatus(0, 1, "Cable", List("Cable"))

    val channels = List(
      Channel(
        1,
        "Test Channel 1",
        s"http://${config.host}:${config.port}/auto/v1"
      )
    )

    HttpRoutes.of[IO] {
      case GET -> Root / "discover.json" =>
        Ok(device.asJson)
      case GET -> Root / "lineup_status.json" =>
        Ok(lineupStatus.asJson)
      case GET -> Root / "lineup.json" =>
        Ok(channels.asJson)
      case GET -> Root / "auto" / channel =>
        val stream = streamForChannel(config, blocker, channel)
        Ok(stream, Header("Content-Type", "video/mpeg"))
    }
  }
}
