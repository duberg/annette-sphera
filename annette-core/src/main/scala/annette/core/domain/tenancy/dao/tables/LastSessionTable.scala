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

import annette.core.domain.tenancy.model.{ LastSession, User }
import com.outworkers.phantom.dsl._

import scala.concurrent.Future
/**
 * Created by valery on 16.12.16.
 */

protected[dao] abstract class LastSessionTable extends Table[LastSessionTable, LastSession] with RootConnector {
  object userId extends UUIDColumn with PartitionKey
  object tenantId extends StringColumn
  object applicationId extends StringColumn
  object languageId extends StringColumn
  object startTimestamp extends DateTimeColumn
  object endTimestamp extends DateTimeColumn
  object ip extends StringColumn
  object id extends UUIDColumn

  override def fromRow(r: Row): LastSession = {
    LastSession(userId(r), tenantId(r), applicationId(r), languageId(r),
      startTimestamp(r), endTimestamp(r), ip(r), id(r))
  }

  override def tableName: String = "core_last_sessions"

  def store(lastSession: LastSession): Future[ResultSet] = {
    insert
      .value(_.id, lastSession.id)
      .value(_.userId, lastSession.userId)
      .value(_.tenantId, lastSession.tenantId)
      .value(_.applicationId, lastSession.applicationId)
      .value(_.languageId, lastSession.languageId)
      .value(_.startTimestamp, lastSession.startTimestamp)
      .value(_.endTimestamp, lastSession.endTimestamp)
      .value(_.ip, lastSession.ip)
      .future()
  }

  def getByUserId(userId: User.Id): Future[Option[LastSession]] = {
    select.where(_.userId eqs userId).one()
  }

  def deleteByUserId(userId: User.Id): Future[ResultSet] = {
    delete.where(_.userId eqs userId).future()
  }

  def selectAll = {
    select.fetch
  }

}

