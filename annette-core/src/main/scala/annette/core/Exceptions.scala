package annette.core

import annette.core.domain.tenancy.model.Tenant
import annette.core.notification.VerifyBySmsNotification
import annette.core.notification.Notification

sealed trait CoreException extends RuntimeException {
  val cause: Option[CoreException] = None
  val code: String
  val parameters: Map[String, String]
  val message: String

  override def getMessage: String = message
  override def getCause: CoreException = cause.orNull
}

case class UnknownException() extends CoreException {
  val code = "core.exceptions.UnknownException"
  val parameters = Map.empty
  val message = s"Unknown exception"
}

/**
 * = NotFoundExceptions =
 */
case class TenantNotFoundException(tenantIds: Set[Tenant.Id]) extends CoreException {
  val code = "core.exceptions.TenantNotFoundException"
  val p1 = tenantIds.mkString(" ,")
  val parameters = Map("tenantIds" -> p1)
  val message = s"Tenant [$p1] not found"
}

case class VerificationNotFoundException(verificationId: Notification.Id) extends CoreException {
  val code = "core.exceptions.VerificationNotFoundException"
  val p1 = verificationId.toString
  val parameters = Map("verificationId" -> p1)
  val message = s"Verification [$p1] not found"
}

case class VerificationInvalidCodeException() extends CoreException {
  val code = "core.exceptions.VerificationInvalidCodeException"
  val parameters = Map.empty
  val message = s"Invalid verification code"
}

/**
 * = AlreadyExistsExceptions =
 */
case class VerificationAlreadyExistsException(verificationId: Notification.Id) extends CoreException {
  val code = "core.exceptions.VerificationAlreadyExistsException"
  val p1 = verificationId.toString
  val parameters = Map("verificationId" -> p1)
  val message = s"Verification [$p1] already exists"
}

case class RequiredValueNotProvided(field: String) extends CoreException {
  val code = "core.exceptions.RequiredValueNotProvided"
  val parameters = Map("field" -> field)
  val message = s"Required value $field not provided"
}