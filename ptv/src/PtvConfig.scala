import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

case class PtvConfig(
  host: String,
  port: Int,
  tunerCount: Int,
  testFile: String
)

object PtvConfig {
  implicit val ptvConfigReader: ConfigReader[PtvConfig] = deriveReader[PtvConfig]
}
