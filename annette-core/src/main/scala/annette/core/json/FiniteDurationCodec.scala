package annette.core.json

import java.util.concurrent.TimeUnit

import io.circe.{ Decoder, Encoder }

import scala.concurrent.duration.{ Duration, FiniteDuration }

trait FiniteDurationCodec {
  implicit val encodeFiniteDuration: Encoder[FiniteDuration] = (x: FiniteDuration) => x.toString().asJson

  implicit val decodeFiniteDuration: Decoder[FiniteDuration] =
    Decoder.decodeString
      .map(Duration.apply(_).toMillis)
      .map(FiniteDuration.apply(_, TimeUnit.MILLISECONDS))
}
