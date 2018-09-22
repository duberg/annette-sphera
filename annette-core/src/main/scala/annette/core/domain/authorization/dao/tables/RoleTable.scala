/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.authorization.dao.tables

import annette.core.domain.authorization.model.{ Permission, PermissionObject, Role }
import annette.core.domain.tenancy.model.Tenant
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class RoleTable extends Table[RoleTable, Role] with RootConnector {

  object tenantId extends StringColumn with PartitionKey
  object roleId extends StringColumn with PrimaryKey
  object description extends StringColumn
  object activated extends BooleanColumn
  object permissions extends SetColumn[String]

  override def fromRow(row: Row): Role = {
    val permissionObjects = toPermissionObjects(permissions(row))
    Role(tenantId(row), roleId(row), description(row), activated(row), permissionObjects)
  }

  private def toPermissionObjects(p: Set[String]): Map[Permission.Id, PermissionObject] = {
    Map(
      p.map { s =>
        val po = toPermissionObject(s)
        (po.permissionId, po)
      }.toList: _*)
  }

  private def toPermissionObject(p: String): PermissionObject = {
    val permissionAndKeys = p.trim.split(":")
    val permissionId = permissionAndKeys(0).trim
    if (permissionAndKeys.length == 1) {
      PermissionObject(permissionId, Set.empty)
    } else {
      val keys = permissionAndKeys(1).split(",").map(_.trim).filter(_.length > 0).toSet
      PermissionObject(permissionId, keys)
    }
  }

  override def tableName: String = "core_roles"

  def store(entity: Role): Future[ResultSet] = {
    insert
      .value(_.tenantId, entity.tenantId)
      .value(_.roleId, entity.roleId)
      .value(_.description, entity.description)
      .value(_.permissions, fromPermissionObjects(entity.permissionObjects))
      .future()
  }

  private def fromPermissionObjects(poMap: Map[Permission.Id, PermissionObject]): Set[String] = {
    poMap
      .values
      .map {
        po =>
          val keys = po.keys.mkString(", ")
          s"${po.permissionId}: ${keys}"
      }
      .toSet
  }

  def updateActivated(tenantId: Tenant.Id, roleId: Role.Id) = {
    update.where(_.tenantId eqs tenantId).and(_.roleId eqs roleId).modify(_.activated setTo true).future()
  }

  def deleteById(tenantId: String, roleId: String): Future[ResultSet] = {
    delete.where(_.tenantId eqs tenantId).and(_.roleId eqs roleId).future()
  }

  def getById(tenantId: String, roleId: String): Future[Option[Role]] = {
    select.where(_.tenantId eqs tenantId).and(_.roleId eqs roleId).one()
  }

  def isExist(tenantId: String, roleId: String): Future[Boolean] = {
    select(_.roleId).where(_.tenantId eqs tenantId).and(_.roleId eqs roleId).one().map(_.isDefined)
  }

  def selectByTenantId(tenantId: String): Future[List[Role]] = {
    select.where(_.tenantId eqs tenantId).fetch()
  }

  def selectAll = {
    select.fetch
  }
}
