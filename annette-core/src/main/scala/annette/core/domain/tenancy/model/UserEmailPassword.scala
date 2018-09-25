package annette.core.domain.tenancy.model

/**
 * Created by valery on 17.12.16.
 */
case class UserEmailPassword(
  email: String,
  password: String,
  userId: User.Id)
