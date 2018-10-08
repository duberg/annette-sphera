package annette.imc

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.Props
import annette.core.domain.tenancy.model.User
import annette.imc.persistence.Persistence._
import annette.imc.model._

class ApsActor(val id: String, val initState: ApsState)
  extends PersistentStateActor[ApsState] {
  override def persistenceId: String = s"imc-aps-$id-2"
  import ApsActor._

  //  override def snapshotInterval: Int = 1

  def create(s: ApsState, user: User.Id): Unit = {

    val id = UUID.randomUUID()
    val status = ApStatus(
      ApStatus.FILLING,
      ZonedDateTime.now(),
      Some(user))
    val ap = Ap(
      id = id,
      apData = ApData(),
      apStatus = status,
      history = Seq(status),
      projectManager = user)

    persist(CreatedEvt(ap)) { event =>
      changeState(s.updated(event))
      sender ! Created(id)
    }
  }

  def delete(s: ApsState, id: Ap.Id): Unit = {
    persist(DeletedEvt(id)) { event =>
      changeState(s.updated(event))
      sender ! Done
    }
  }

  def fillingForm(state: ApsState, ap: UpdateAp): Unit = {

    state.getApOpt(ap.id) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        persist(FillingFormEvt(ap)) { event =>
          changeState(state.updated(event))
          sender ! Done
        }
    }
  }

  def addFile(state: ApsState, id: Ap.Id, file: ApFile): Unit = {
    state.getApOpt(id) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        persist(AddFileEvt(id, file)) { event =>
          changeState(state.updated(event))
          sender ! Done
        }
    }
  }

  def updateFile(state: ApsState, id: Ap.Id, file: ApFile): Unit = {
    state.getApOpt(id) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        if (x.apFiles.get(file.id).isEmpty) sender ! FileNotExists
        else {
          persist(UpdateFileEvt(id, file)) { event =>
            changeState(state.updated(event))
            sender ! Done
          }
        }
    }
  }

  def removeFile(state: ApsState, apId: Ap.Id, id: ApFile.Id): Unit = {
    state.getApOpt(apId) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        if (x.apFiles.get(id).isEmpty) sender ! FileNotExists

        persist(RemoveFileEvt(apId, id)) { event =>
          changeState(state.updated(event))
          sender ! Done
        }
    }
  }

  def updateCriterion(state: ApsState, id: Ap.Id, criterion: UpdateCriterion): Unit = {

    def updateRu(a: ApString): ApString = {
      criterion.descriptionRu match {
        case Some(x) => a.copy(ru = x)
        case None => a.copy()
      }
    }

    def updateEn(a: ApString): ApString = {
      criterion.descriptionEn match {
        case Some(x) => a.copy(en = x)
        case None => a.copy()
      }
    }

    def updatedD = updateRu _ andThen updateEn

    state.getApOpt(id) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        persist(UpdateCriterionEvt(id, criterion)) { event =>
          changeState(state.updated(event))
          sender ! Done
        }
    }
  }

  def toggleCriterion(state: ApsState, apId: Ap.Id, id: Int): Unit = {
    state.getApOpt(apId) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        x.criterions.get(id) match {
          case Some(y) =>
            val apCopy = x.copy(criterions = x.criterions +
              (id -> y.copy(isFinished = !y.isFinished)))

            persist(FinishCriterionEvt(apId, id)) { event =>
              changeState(state.updated(event))
              sender ! Done
            }
          case None => sender ! CriterionNotExists
        }
    }
  }

  def addCriterionFile(state: ApsState, apId: Ap.Id, cId: Int, fId: ApFile.Id): Unit = {

    state.getApOpt(apId) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        if (x.apFiles.get(fId).isEmpty) sender ! FileNotExists
        else x.criterions.get(cId) match {
          case Some(y) =>
            val apCopy = x.copy(criterions = x.criterions +
              (cId -> y.copy(attachment = y.attachment + fId)))

            persist(AddCriterionFileEvt(apId, cId, fId)) { event =>
              changeState(state.updated(event))
              sender ! Done
            }
          case None => sender ! CriterionNotExists
        }
    }
  }

  def removeCriterionFile(state: ApsState, apId: Ap.Id, cId: Int, fId: ApFile.Id): Unit = {

    state.getApOpt(apId) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        if (x.apFiles.get(fId).isEmpty) sender ! FileNotExists
        else x.criterions.get(cId) match {
          case Some(y) =>
            val apCopy = x.copy(criterions = x.criterions +
              (cId -> y.copy(attachment = y.attachment - fId)))

            persist(RemoveCriterionFileEvt(apId, cId, fId)) { event =>
              changeState(state.updated(event))
              sender ! Done
            }
          case None => sender ! CriterionNotExists
        }
    }
  }

  def cleanCriterion(state: ApsState, apId: Ap.Id, id: Int): Unit = {
    state.getApOpt(apId) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        x.criterions.get(id) match {
          case Some(y) =>
            val apCopy = x.copy(criterions = x.criterions - id)
            persist(CleanCriterionEvt(apId, id)) { event =>
              changeState(state.updated(event))
              sender ! Done
            }
          case None => sender ! CriterionNotExists
        }
    }
  }

  private def fillingStatus(state: ApsState, status: ApStatus, ap: Ap): Unit = {
    val apCopy = ap.copy(apStatus = status, history = ap.history :+ status)
    persist(ChangeStatusEvt(ap.id, status)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  private def filledStatus(state: ApsState, status: ApStatus, ap: Ap): Unit = {

    val apCopy = ap.copy(apStatus = status, history = ap.history :+ status)
    persist(ChangeStatusEvt(ap.id, status)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  private def accomplishedStatus(state: ApsState, status: ApStatus, ap: Ap): Unit = {

    val apCopy = ap.copy(apStatus = status, history = ap.history :+ status)
    persist(ChangeStatusEvt(ap.id, status)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  private def readyStatus(state: ApsState, status: ApStatus, ap: Ap): Unit = {

    val apCopy = ap.copy(apStatus = status, history = ap.history :+ status)
    persist(ChangeStatusEvt(ap.id, status)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  private def onExpertiseStatus(state: ApsState, status: ApStatus, ap: Ap): Unit = {
    val apCopy = ap.copy(apStatus = status, history = ap.history :+ status)
    persist(ChangeStatusEvt(ap.id, status)) { event =>
      changeState(state.updated(event))
      sender ! Done
    }
  }

  def changeStatus(state: ApsState, id: Ap.Id, status: ApStatus): Unit = {

    state.storage.aps.get(id) match {
      case None => sender ! ApNotExists
      case Some(x) if status.nameMessage == ApStatus.FILLING => fillingStatus(state, status, x)
      case Some(x) if status.nameMessage == ApStatus.FILLED => filledStatus(state, status, x)
      case Some(x) if status.nameMessage == ApStatus.READY => readyStatus(state, status, x)
      case Some(x) if status.nameMessage == ApStatus.ACCOMPLISHED => accomplishedStatus(state, status, x)
      case Some(x) if status.nameMessage == ApStatus.ONEXPERTISE => onExpertiseStatus(state, status, x)
    }
  }

  def addExpert(state: ApsState, apId: Ap.Id, userId: User.Id): Unit = {
    state.storage.aps.get(apId) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        persist(AddExpertEvt(apId, userId)) { event =>
          changeState(state.updated(event))
          sender ! Done
        }
    }

  }

  def removeExpert(state: ApsState, apId: Ap.Id, userId: User.Id): Unit = {
    state.storage.aps.get(apId) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        x.expertise.bulletins.get(userId) match {
          case Some(y) =>
            persist(RemoveExpertEvt(apId, userId)) { event =>
              changeState(state.updated(event))
              sender ! Done
            }
          case None => sender ! NotFound
        }

    }
  }

  def updateBulletin(state: ApsState, apId: Ap.Id, bulletin: UpdateBulletin): Unit = {

    state.storage.aps.get(apId) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        x.expertise.bulletins.get(bulletin.expertId) match {
          case Some(y) =>
            persist(UpdateBulletinEvt(apId, bulletin)) { event =>
              changeState(state.updated(event))
              sender ! Done
            }
          case None => sender ! NotFound
        }

    }
  }

  def vote(state: ApsState, apId: Ap.Id, userId: User.Id): Unit = {
    state.storage.aps.get(apId) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        x.expertise.bulletins.get(userId) match {
          case Some(y) =>
            val bulletin: Bulletin = y.copy(isFinished = true)
            val expertise: Expertise = x.expertise.copy(
              bulletins = x.expertise.bulletins + (bulletin.expertId -> bulletin))
            if (expertise.bulletins.values.forall(_.isFinished)) {
              val status = ApStatus(
                ApStatus.ACCOMPLISHED,
                ZonedDateTime.now(),
                None,
                None,
                Some(ApResult()))
              self ! ChangeStatusCmd(apId, status)
            }

            persist(VoteEvt(apId, userId)) { event =>
              changeState(state.updated(event))
              sender ! Done
            }
          case None => sender ! NotFound
        }

    }
  }

  def changeManager(state: ApsState, apId: Ap.Id, managerId: User.Id): Unit = {
    state.storage.aps.get(apId) match {
      case None => sender ! ApNotExists
      case Some(ap) =>
        val apCopy = ap.copy(projectManager = managerId)
        persist(ChangeManagerEvt(apId, managerId)) { event =>
          changeState(state.updated(event))
          sender ! Done
        }
    }
  }

  def getById(state: ApsState, id: Ap.Id): Unit = {
    state.storage.aps.get(id) match {
      case None => sender ! ApNotExists
      case Some(x) =>
        sender ! ApFound(x)
    }
  }

  def find(state: ApsState, params: SearchParams): Unit = {

    val testApString = (text: String, as: Option[ApString]) => as match {
      case Some(n) if n.ru.toLowerCase().startsWith(text.toLowerCase()) || n.en.toLowerCase().startsWith(text.toLowerCase()) =>
        true
      case _ => false
    }

    params match {
      case SearchParams(None, None) =>
        val found = state.storage.aps.values.toSet
        sender ! ApsFound(found)
      case SearchParams(Some(x), None) =>
        val found = state.storage.aps.values.filter(a =>
          testApString(x, a.apData.entityName) || testApString(x, a.apData.personName)).toSet
        sender ! ApsFound(found)
      case SearchParams(None, Some(y)) =>
        val found = state.storage.aps.values.filter(_.apStatus.nameMessage == y).toSet
        sender ! ApsFound(found)
      case SearchParams(Some(x), Some(y)) =>
        val found = state.storage.aps.values.filter(a =>
          (testApString(x, a.apData.entityName) || testApString(x, a.apData.personName)) &&
            a.apStatus.nameMessage == y).toSet
        sender ! ApsFound(found)
    }
  }

  def getCriterionById(state: ApsState, apId: Ap.Id, id: Int): Unit = {
    state.storage.aps.get(apId) match {
      case None => sender ! ApNotExists
      case Some(x) => x.criterions.get(id) match {
        case Some(y) => sender() ! CriterionFound(y)
        case None => sender ! CriterionNotExists
      }
    }
  }

  def findByExpert(state: ApsState, id: User.Id): Unit = {
    val found = state.storage.aps.values.filter(_.expertise.experts.contains(id)).toSet
    sender ! ApsFound(found)
  }

  def behavior(state: ApsState): Receive = {
    case CreateCmd(user) => create(state, user)
    case RemoveCmd(apId) => delete(state, apId)
    case FillingFormCmd(upd) => fillingForm(state, upd)
    case AddFileCmd(apId, file) => addFile(state, apId, file)
    case UpdateFileCmd(apId, file) => updateFile(state, apId, file)
    case RemoveFileCmd(apId, fileId) => removeFile(state, apId, fileId)
    case UpdateCriterionCmd(apId, criterion) => updateCriterion(state, apId, criterion)
    case FinishCriterionCmd(apId: Ap.Id, criterionId: Int) =>
      toggleCriterion(state, apId, criterionId)
    case AddCriterionFileCmd(apId, criterionId, fileId) =>
      addCriterionFile(state, apId, criterionId, fileId)
    case RemoveCriterionFileCmd(apId, criterionId, fileId) =>
      removeCriterionFile(state, apId, criterionId, fileId)
    case CleanCriterionCmd(apId, cId) => cleanCriterion(state, apId, cId)
    case ChangeStatusCmd(apId, status) => changeStatus(state, apId, status)
    case AddExpertCmd(apId, userId) => addExpert(state, apId, userId)
    case RemoveExpertCmd(apId, userId) => removeExpert(state, apId, userId)
    case UpdateBulletinCmd(apId, bulletin) => updateBulletin(state, apId, bulletin)
    case VoteCmd(apId, expertId) => vote(state, apId, expertId)
    case ChangeManagerCmd(apId, managerId) => changeManager(state, apId, managerId)
    case GetApById(apId: Ap.Id) => getById(state, apId)
    case FindAps(params: SearchParams) => find(state, params)
    case GetCriterionById(apId, criterionId) => getCriterionById(state, apId, criterionId)
    case FindApsByExpert(expert) => findByExpert(state, expert)
  }
}

object ApsActor {
  trait Command extends PersistentCommand
  trait Request extends PersistentQuery
  trait Response extends PersistentResponse
  trait Event extends PersistentEvent

  case class CreateCmd(user: User.Id) extends Command
  case class RemoveCmd(apId: Ap.Id) extends Command
  case class FillingFormCmd(update: UpdateAp) extends Command
  case class AddFileCmd(ap: Ap.Id, file: ApFile) extends Command
  case class ChangeManagerCmd(ap: Ap.Id, manager: User.Id) extends Command
  case class UpdateFileCmd(ap: Ap.Id, file: ApFile) extends Command
  case class RemoveFileCmd(ap: Ap.Id, fileId: ApFile.Id) extends Command
  case class UpdateCriterionCmd(ap: Ap.Id, criterion: UpdateCriterion) extends Command
  case class FinishCriterionCmd(ap: Ap.Id, criterionId: Int) extends Command
  case class AddCriterionFileCmd(ap: Ap.Id, criterionId: Int, fileId: ApFile.Id) extends Command
  case class RemoveCriterionFileCmd(ap: Ap.Id, criterionId: Int, fileId: ApFile.Id) extends Command
  case class CleanCriterionCmd(ap: Ap.Id, criterionId: Int) extends Command
  case class ChangeStatusCmd(ap: Ap.Id, status: ApStatus) extends Command
  case class AddExpertCmd(ap: Ap.Id, userId: User.Id) extends Command
  case class RemoveExpertCmd(ap: Ap.Id, userId: User.Id) extends Command
  case class UpdateBulletinCmd(ap: Ap.Id, bulletin: UpdateBulletin) extends Command
  case class VoteCmd(ap: Ap.Id, expert: User.Id) extends Command

  case class GetApById(ap: Ap.Id) extends Request
  case class FindAps(params: SearchParams) extends Request
  case class FindApsByExpert(expert: User.Id) extends Request
  case class GetCriterionById(ap: Ap.Id, criterionId: Int) extends Request

  case class CreatedEvt(ap: Ap) extends Event
  case class UpdatedEvt(ap: Ap) extends Event
  case class DeletedEvt(ap: Ap.Id) extends Event

  // new Events
  case class FillingFormEvt(update: UpdateAp) extends Event
  case class UpdateFileEvt(apId: Ap.Id, file: ApFile) extends Event
  case class AddFileEvt(ap: Ap.Id, file: ApFile) extends Event
  case class RemoveFileEvt(ap: Ap.Id, fileId: ApFile.Id) extends Event
  case class UpdateCriterionEvt(ap: Ap.Id, criterion: UpdateCriterion) extends Event
  case class FinishCriterionEvt(ap: Ap.Id, criterionId: Int) extends Event
  case class AddCriterionFileEvt(ap: Ap.Id, criterionId: Int, fileId: ApFile.Id) extends Event
  case class RemoveCriterionFileEvt(ap: Ap.Id, criterionId: Int, fileId: ApFile.Id) extends Event
  case class CleanCriterionEvt(ap: Ap.Id, criterionId: Int) extends Event
  case class ChangeStatusEvt(ap: Ap.Id, status: ApStatus) extends Event
  case class AddExpertEvt(ap: Ap.Id, userId: User.Id) extends Event
  case class RemoveExpertEvt(ap: Ap.Id, userId: User.Id) extends Event
  case class UpdateBulletinEvt(ap: Ap.Id, bulletin: UpdateBulletin) extends Event
  case class VoteEvt(ap: Ap.Id, expert: User.Id) extends Event
  case class ChangeManagerEvt(ap: Ap.Id, manager: User.Id) extends Event
  //

  case class Created(id: Ap.Id) extends Response
  case object Done extends Response
  case object ApNotExists extends Response
  case object FileNotExists extends Response
  case object CriterionNotExists extends Response
  //  case object CriterionFileNotExists extends Response
  case object EnterEntityNameFirst extends Response
  case object NotFound extends Response
  case class ApFound(ap: Ap) extends Response
  case class ApsFound(aps: Set[Ap]) extends Response
  case class CriterionFound(criterion: Criterion) extends Response

  def props(id: String, state: ApsState) = Props(new ApsActor(id, state))
}
