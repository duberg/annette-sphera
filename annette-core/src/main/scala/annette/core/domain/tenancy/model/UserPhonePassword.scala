package annette.core.domain.tenancy.model

case class UserPhonePassword(
  phone: String,
  password: String,
  userId: User.Id)
