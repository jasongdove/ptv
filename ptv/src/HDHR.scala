import io.circe.generic.extras._

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
}
