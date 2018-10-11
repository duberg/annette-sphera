package annette.core.domain.tenancy.model

case class UserEmailPassword(
  email: String,
  password: String,
  userId: User.Id)
