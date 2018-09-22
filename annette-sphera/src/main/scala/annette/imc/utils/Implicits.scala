package annette.imc.utils

import java.nio.file.Paths
import java.time.ZonedDateTime

import akka.http.scaladsl.model.{ HttpEntity, MediaTypes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl._
import io.circe.Decoder.Result
import io.circe.{ Decoder, Encoder, HCursor, Json }

object Implicits {
  implicit val decodeZonedDateTime: Decoder[ZonedDateTime] = new Decoder[ZonedDateTime] {
    final def apply(t: HCursor): Result[ZonedDateTime] = for {
      a <- t.as[String]
    } yield ZonedDateTime.parse(a)
  }

  implicit val encodeFoo: Encoder[ZonedDateTime] = new Encoder[ZonedDateTime] {
    final def apply(a: ZonedDateTime): Json = Json.fromString(a.toString)
  }
}

object Files {
  def downloadFile(file: String): Route = {
    val f = Paths.get(file)
    val responseEntity = HttpEntity(
      MediaTypes.`application/octet-stream`,
      Paths.get(file).toFile.length(),
      FileIO.fromPath(f, 262144))

    complete(responseEntity)
  }
}
