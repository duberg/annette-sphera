package annette.core.security.authorization

import java.util

import akka.actor.Status.Failure
import akka.actor.{ Actor, ActorLogging, Props }
import annette.core.security.authorization.AuthorizationActor._
import javax.inject.Named
import org.casbin.jcasbin.main.Enforcer

import scala.collection.JavaConverters._
import scala.concurrent.Future

@Named("AuthorizationManager")
class AuthorizationActor(enforcer: Enforcer) extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg: ValidateAuthorizedUser =>
      validateUserAuthorization(msg)

    case msg: CreatePolicy =>
      processCreatePolicy(msg)
    case msg: ReadPolicy =>
      processReadPolicy(msg)
    case msg: DeleteRole =>
      processDeleteRole(msg)
    case msg: DeletePermissions =>
      processDeletePermission(msg)

    case msg: ReadAuthrUser =>
      processReadAuthrUser(msg)
    case msg: CreateAuthrUser =>
      processCreateAuthrUser(msg)
    case msg: PatchAuthrUser =>
      processPatchAuthrUser(msg)
    case msg: DeleteAuthrUser =>
      processDeleteAuthrUser(msg)

    case _ =>
    // logger.warn(s"Unknown msg received.")
  }

  def validateUserAuthorization(msg: ValidateAuthorizedUser): Unit = {
    try {
      log.info(msg.toString)

      val userId = msg.userId.getOrElse("")
      val accessPath = msg.accessPath.getOrElse("")
      val action = msg.action.getOrElse("")

      println(enforcer.getRolesForUser(msg.userId.get))

      val roles = enforcer.getRolesForUser(msg.userId.get).asScala

      val result: Boolean = enforcer.enforce(userId, accessPath, action).booleanValue()

      sender() ! result
    } catch {
      case e: Throwable =>
        // logger.error(s"Error Occured $e")
        sender() ! Failure(e)
    }
  }

  def processCreatePolicy(msg: CreatePolicy): Unit = {
    try {
      ParamValidationUtil.validateActions(msg.actions)
      enforcer.addPermissionForUser(msg.roleName, msg.accessPath, msg.actions.mkString("|").toUpperCase)
      enforcer.savePolicy()

      sender() ! Future.successful(msg.roleName)
    } catch {
      case e: Throwable =>
        //logger.error(s"Error Occured $e")
        sender() ! Failure(e)
    }
  }

  def processReadPolicy(msg: ReadPolicy): Unit = {
    val origSender = sender()

    try {
      val permissionList = enforcer.getPermissionsForUser(msg.roleName.getOrElse(""))
      var resultSeq: Seq[AuthrReqUser] = Seq()
      permissionList forEach {
        case permissions: java.util.List[String] => {
          val seq: Seq[String] = permissions.asScala
          resultSeq = resultSeq :+ AuthrReqUser(seq(0), seq(1), seq(2))
        }
      }
      origSender ! resultSeq
    } catch {
      case e: Throwable =>
        // logger.error(s"Error Occured $e")
        origSender ! Failure(e)
    }
  }

  def processDeleteRole(msg: DeleteRole): Unit = {
    try {
      enforcer.deleteRole(msg.roleName)
      enforcer.savePolicy()
      sender() ! Future.successful()
    } catch {
      case e: Throwable =>
        // logger.error(s"Error Occured $e")
        sender() ! Failure(e)
    }
  }

  def processDeletePermission(msg: DeletePermissions): Unit = {
    try {

      val result: Boolean = enforcer.deletePermissionForUser(msg.roleName, msg.accessPath, msg.action).booleanValue()
      enforcer.savePolicy()

      sender() ! result
    } catch {
      case e: Throwable =>
        // logger.error(s"Error Occured $e")
        sender() ! false
    }
  }

  def processReadAuthrUser(msg: ReadAuthrUser): Unit = {
    try {
      val roleList: util.List[String] = enforcer.getRolesForUser(msg.userId)
      var resultRoleSeq: Seq[String] = Seq()

      roleList forEach {
        case role: String =>
          resultRoleSeq = resultRoleSeq :+ role
      }

      sender() ! resultRoleSeq
    } catch {
      case e: Throwable =>
        // logger.error(s"Error Occured $e")
        sender() ! Failure(e)
    }

  }

  def processPatchAuthrUser(msg: PatchAuthrUser): Unit = {
    try {

      var result = enforcer.deleteUser(msg.user.userId).booleanValue()
      if (result)
        result = enforcer.addRoleForUser(msg.user.userId, msg.user.roleName).booleanValue()

      enforcer.savePolicy()
      sender() ! result
    } catch {
      case e: Throwable =>
        // logger.error(s"Error Occured $e")
        sender() ! Failure(e)
    }
  }

  def processDeleteAuthrUser(msg: DeleteAuthrUser): Unit = {
    val origSender = sender()

    try {
      val result: Boolean = enforcer.deleteUser(msg.userId).booleanValue()
      enforcer.savePolicy()
      origSender ! result
    } catch {
      case e: Throwable =>
        //logger.error(s"Error Occured $e")
        origSender ! Failure(e)
    }
  }
  def processCreateAuthrUser(msg: CreateAuthrUser): Unit = {
    try {
      val result: Boolean = enforcer.addRoleForUser(msg.uesr.userId, msg.uesr.roleName).booleanValue()
      enforcer.savePolicy()
      sender() ! result
    } catch {
      case e: Throwable =>
        // logger.error(s"Error Occured $e")
        sender() ! Failure(e)
    }
  }
}

object AuthorizationActor {
  def props(enforcer: Enforcer) = Props(new AuthorizationActor(enforcer))

  case class CreatePolicy(roleName: String, accessPath: String, actions: Seq[String])

  case class ReadPolicy(roleName: Option[String], order: Option[String])

  case class PatchPolicy(roleName: String, accessPath: String, actions: Seq[String])

  case class DeletePermissions(roleName: String, accessPath: String, action: String)

  case class DeleteRole(roleName: String)

  case class ValidateAuthorizedUser(userId: Option[String], accessPath: Option[String], action: Option[String])

  case class CreateAuthrUser(uesr: AuthrUser)

  case class ReadAuthrUser(userId: String)

  case class PatchAuthrUser(user: AuthrUser)

  case class DeleteAuthrUser(userId: String)

}
