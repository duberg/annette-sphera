package annette.imc.model

import java.time.ZonedDateTime

import annette.core.domain.tenancy.model.User

case class ApStatus(
  nameMessage: String,
  changeTime: ZonedDateTime,
  userId: Option[User.Id],
  comment: Option[String] = None,
  result: Option[ApResult] = None)

object ApStatus {
  private val root = ""
  val FILLING: String = root + "filling"
  val FILLED: String = root + "filled"
  val READY: String = root + "ready"
  val ONEXPERTISE: String = root + "onexpertise"
  val ACCOMPLISHED: String = root + "accomplished"
  val ARCHIVE: String = root + "archive"

  def make(
    shortName: String,
    changeTime: ZonedDateTime,
    userId: Option[User.Id],
    comment: Option[String],
    result: Option[ApResult] = None): ApStatus = ApStatus(root + shortName, changeTime, userId, comment, result)
}

case class ApComment(comment: String)
