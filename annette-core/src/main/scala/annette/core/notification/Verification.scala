package annette.core.notification

import java.util.UUID

import scala.concurrent.duration.{ FiniteDuration, _ }

case class Verification(
                         id: Verification.Id,
                         code: String,
                         duration: FiniteDuration)

case class CreateVerification(
                         code: String,
                         duration: FiniteDuration)

object Verification {
  type Id = UUID
}