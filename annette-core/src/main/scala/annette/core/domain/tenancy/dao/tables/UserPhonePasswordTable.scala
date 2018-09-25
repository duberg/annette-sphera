package annette.core.domain.tenancy.dao.tables

import annette.core.domain.tenancy.model.{ User, UserPhonePassword }
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class UserPhonePasswordTable extends Table[UserPhonePasswordTable, UserPhonePassword] with RootConnector {
  object phone extends StringColumn with PartitionKey
  object password extends StringColumn
  object userId extends UUIDColumn

  override def fromRow(row: Row): UserPhonePassword = {
    UserPhonePassword(
      phone = phone(row),
      password = password(row),
      userId = userId(row))
  }

  override def tableName: String = "core_user_phone_passwords"

  def store(entity: UserPhonePassword): Future[ResultSet] = {
    insert.value(_.phone, entity.phone)
      .value(_.userId, entity.userId)
      .value(_.password, entity.password)
      .future()
  }

  def store(user: User, password: String): Future[ResultSet] = {
    insert.value(_.phone, user.phone.get)
      .value(_.userId, user.id)
      .value(_.password, password)
      .future()
  }

  def getByPhone(phone: String): Future[Option[UserPhonePassword]] = {
    select.where(_.phone eqs phone).one()
  }

  def deleteByPhone(phone: String): Future[ResultSet] = {
    delete.where(_.phone eqs phone).future()
  }

  def isExist(phone: String): Future[Boolean] = {
    select(_.phone).where(_.phone eqs phone).one().map(_.isDefined)
  }

  /*override def autocreate(space: KeySpace): CreateQuery.Default[UserPhonePasswordTable, UserPhonePassword] = {
    create.ifNotExists()(space).`with`(default_time_to_live eqs 10)
      .and(gc_grace_seconds eqs 10.seconds)
      .and(read_repair_chance eqs 0.2)
  }*/
}
