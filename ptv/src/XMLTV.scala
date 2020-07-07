import cats.effect.{Blocker, ContextShift, IO}
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, StaticFile}

object XMLTV {
  def apply(blocker: Blocker)(
    implicit cs: ContextShift[IO]
  ): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case request @ GET -> Root / "xmltv.xml" =>
        StaticFile
          .fromResource("/xmltv.xml", blocker, Some(request))
          .getOrElseF(NotFound())
    }
}
