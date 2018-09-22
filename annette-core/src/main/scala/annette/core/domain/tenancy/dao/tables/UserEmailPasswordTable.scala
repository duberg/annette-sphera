/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.tenancy.dao.tables

import annette.core.domain.tenancy.model.{ User, UserEmailPassword }
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class UserEmailPasswordTable extends Table[UserEmailPasswordTable, UserEmailPassword] with RootConnector {
  object email extends StringColumn with PartitionKey
  object password extends StringColumn
  object userId extends UUIDColumn

  override def fromRow(row: Row): UserEmailPassword = {
    UserEmailPassword(
      email = email(row),
      password = password(row),
      userId = userId(row))
  }

  override def tableName: String = "core_user_email_passwords"

  def store(entity: UserEmailPassword): Future[ResultSet] = {
    insert.value(_.email, entity.email)
      .value(_.userId, entity.userId)
      .value(_.password, entity.password)
      .future()
  }

  def store(user: User, password: String): Future[ResultSet] = {
    insert.value(_.email, user.email.get)
      .value(_.userId, user.id)
      .value(_.password, password)
      .future()
  }

  def getByEmail(email: String): Future[Option[UserEmailPassword]] = {
    select.where(_.email eqs email).one()
  }

  def deleteByEmail(email: String): Future[ResultSet] = {
    delete.where(_.email eqs email).future()
  }

  def isExist(email: String): Future[Boolean] = {
    select(_.email).where(_.email eqs email).one().map(_.isDefined)
  }

  /* override def autocreate(space: KeySpace): CreateQuery.Default[UserEmailPasswordTable, UserEmailPassword] = {
    create.ifNotExists()(space).`with`(default_time_to_live eqs 10)
      .and(gc_grace_seconds eqs 10.seconds)
      .and(read_repair_chance eqs 0.2)
  }*/
}
