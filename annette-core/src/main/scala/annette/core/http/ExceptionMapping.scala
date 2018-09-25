package annette.core.http

import annette.core.CoreException
import annette.core.exception.AnnetteException
//
//import annette.bpm.Exception._
//import annette.bpm.model.EntityType
//import annette.core.exception.AnnetteException
//

trait ExceptionMapping {
  implicit def toAnnetteException(exception: Throwable): AnnetteException = {
    exception match {
      //      case e: UnknownException =>
      //        new AnnetteException("bpm2.exceptions.UnknownException")

      //      case e: EntityNotFoundException =>
      //        val code = e.entityType match {
      //          case EntityType.DataStructure => "bpm2.exceptions.DataStructureNotFoundException"
      //          case EntityType.ProcessTemplate => "bpm2.exceptions.ProcessTemplateNotFoundException"
      //          case EntityType.ProjectTemplate => "bpm2.exceptions.ProjectTemplateNotFoundException"
      //          case EntityType.FormTemplate => "bpm2.exceptions.FormTemplateNotFoundException"
      //          case EntityType.RoleType => "bpm2.exceptions.RoleTypeNotFoundException"
      //          case EntityType.FileType => "bpm2.exceptions.FileTypeNotFoundException"
      //          case EntityType.CalendarType => "bpm2.exceptions.CalendarTypeNotFoundException"
      //          case EntityType.Characteristic => "bpm2.exceptions.CharacteristicNotFoundException"
      //          case EntityType.ProcessInfo => "bpm2.exceptions.ProcessInfoNotFoundException"
      //          case EntityType.TaskInfo => "bpm2.exceptions.TaskInfoNotFoundException"
      //          case EntityType.Verification => "bpm2.exceptions.VerifyException"
      //        }
      //        new AnnetteException(code, Map("entityId" -> e.entityId))
      //
      //      case e: EntityAlreadyExistsException =>
      //        val code = e.entityType match {
      //          case EntityType.DataStructure => "bpm2.exceptions.DataStructureAlreadyExistsException"
      //          case EntityType.ProcessTemplate => "bpm2.exceptions.ProcessTemplateAlreadyExistsException"
      //          case EntityType.ProjectTemplate => "bpm2.exceptions.ProjectTemplateAlreadyExistsException"
      //          case EntityType.FormTemplate => "bpm2.exceptions.FormTemplateAlreadyExistsException"
      //          case EntityType.RoleType => "bpm2.exceptions.RoleTypeAlreadyExistsException"
      //          case EntityType.FileType => "bpm2.exceptions.FileTypeAlreadyExistsException"
      //          case EntityType.CalendarType => "bpm2.exceptions.CalendarTypeAlreadyExistsException"
      //          case EntityType.Characteristic => "bpm2.exceptions.CharacteristicAlreadyExistsException"
      //        }
      //        new AnnetteException(code, Map("entityId" -> e.entityId))
      //
      //      case e: DataConsistencyException =>
      //        new AnnetteException("bpm2.exceptions.DataConsistencyException", Map("path" -> e.path))
      //
      //      case e: OperationException =>
      //        new AnnetteException(
      //          code = "bpm2.exceptions.OperationException",
      //          params = Map(
      //            "dateTime" -> e.dateTime.toString,
      //            "operationId" -> e.operationId,
      //            "operationBpmnId" -> e.operationBpmId,
      //            "operationName" -> e.operationName),
      //          cause = e.cause.map(toAnnetteException))
      //
      //      case e: UpdateValueException =>
      //        new AnnetteException(
      //          code = "bpm2.exceptions.UpdateValueException",
      //          params = Map("dateTime" -> e.dateTime.toString),
      //          cause = e.cause.map(toAnnetteException))
      //
      //      case e: VerifyException =>
      //        new AnnetteException("bpm2.exceptions.VerifyException")

      case e: AnnetteException => e

      case e => new AnnetteException("bpm2.exceptions.UnknownException")
    }
  }
}