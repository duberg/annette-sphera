///***************************************************************************************
//* Copyright (c) 2014-2017 by Valery Lobachev
//* Redistribution and use in source and binary forms, with or without
//* modification, are NOT permitted without written permission from Valery Lobachev.
//*
//* Copyright (c) 2014-2017 Валерий Лобачев
//* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
//* запрещено без письменного разрешения правообладателя.
//****************************************************************************************/
//
//package annette.core.domain.tenancy.dao.tables
//
//import annette.core.domain.tenancy.model.User
//import com.outworkers.phantom.dsl._
//
//import scala.concurrent.Future
//
///**
// * Created by valery on 16.12.16.
// */
//
//protected[dao] abstract class UserTable extends Table[UserTable, User] with RootConnector {
//  object firstName extends StringColumn
//  object middleName extends StringColumn
//  object lastName extends StringColumn
//  object email extends OptionalStringColumn
//  object phone extends OptionalStringColumn
//  object login extends OptionalStringColumn
//  object defaultLanguage extends StringColumn
//  object id extends UUIDColumn with PartitionKey
//
//  override def fromRow(row: Row): User = {
//    User(
//      firstName = firstName(row),
//      middleName = middleName(row),
//      lastName = lastName(row),
//      email = email(row),
//      phone = phone(row),
//      username = login(row),
//      defaultLanguage = defaultLanguage(row),
//      id = id(row))
//  }
//
//  override def tableName: String = "core_users"
//
//  def store(user: User): Future[ResultSet] = {
//    insert.value(_.id, user.id)
//      .value(_.firstName, user.firstName)
//      .value(_.middleName, user.middleName)
//      .value(_.lastName, user.lastName)
//      .value(_.email, user.email)
//      .value(_.phone, user.phone)
//      .value(_.login, user.username)
//      .value(_.defaultLanguage, user.defaultLanguage)
//      .future()
//  }
//
//  def getById(id: User.Id): Future[Option[User]] = {
//    select.where(_.id eqs id).one()
//  }
//
//  def deleteById(id: User.Id): Future[ResultSet] = {
//    delete.where(_.id eqs id).future()
//  }
//
//  def isExist(id: User.Id): Future[Boolean] = {
//    select(_.id).where(_.id eqs id).one().map(_.isDefined)
//  }
//  def selectAll = {
//    select.fetch
//  }
//
//  /*override def autocreate(space: KeySpace): CreateQuery.Default[UserTable, User] = {
//    create.ifNotExists()(space).`with`(default_time_to_live eqs 10)
//      .and(gc_grace_seconds eqs 10.seconds)
//      .and(read_repair_chance eqs 0.2)
//  }*/
//}
//
