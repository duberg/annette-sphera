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

import annette.core.domain.tenancy.model.SessionHistory
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 16.12.16.
 */

protected[dao] abstract class SessionHistoryTable extends Table[SessionHistoryTable, SessionHistory] with RootConnector {
  object userId extends UUIDColumn
  object tenantId extends StringColumn with PartitionKey
  object applicationId extends StringColumn
  object languageId extends StringColumn
  object startTimestamp extends DateTimeColumn
  object endTimestamp extends DateTimeColumn with ClusteringOrder with Descending
  object ip extends StringColumn
  object id extends UUIDColumn with PrimaryKey with ClusteringOrder with Ascending

  override def fromRow(r: Row): SessionHistory = {
    SessionHistory(userId(r), tenantId(r), applicationId(r), languageId(r),
      startTimestamp(r), endTimestamp(r), ip(r), id(r))
  }

  override def tableName: String = "core_session_histories"

  def store(sessionHistory: SessionHistory): Future[ResultSet] = {
    insert
      .value(_.tenantId, sessionHistory.tenantId)
      .value(_.endTimestamp, sessionHistory.endTimestamp)
      .value(_.id, sessionHistory.id)
      .value(_.userId, sessionHistory.userId)
      .value(_.applicationId, sessionHistory.applicationId)
      .value(_.languageId, sessionHistory.languageId)
      .value(_.startTimestamp, sessionHistory.startTimestamp)
      .value(_.ip, sessionHistory.ip)
      .future()
  }

  def selectAll = {
    select.fetch
  }

}

