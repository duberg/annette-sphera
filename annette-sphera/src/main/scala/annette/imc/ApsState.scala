package annette.imc

import java.time.ZonedDateTime

import annette.core.domain.tenancy.model.User
import annette.imc.persistence.Persistence.{ PersistentEvent, PersistentState }
import annette.imc.ApsActor.{ ChangeStatusCmd, FinishCriterionEvt }
import annette.imc.model._

case class ApsStorage(aps: Map[Ap.Id, Ap] = Map.empty)

case class ApsState(storage: ApsStorage) extends PersistentState[ApsState] {

  private def create(c: Ap): ApsState = {
    copy(
      storage.copy(
        aps = storage.aps + (c.id -> c)))
  }

  private def update(u: Ap): ApsState = {
    copy(
      storage.copy(
        aps = storage.aps + (u.id -> u)))

  }

  private def delete(id: Ap.Id): ApsState = {
    copy(
      storage.copy(
        aps = storage.aps - id))
  }

  def exists(id: Ap.Id): Boolean = {
    storage.aps.get(id).isDefined
  }

  def getApOpt(id: Ap.Id): Option[Ap] = storage.aps.get(id)

  private def fillingForm(ap: UpdateAp): ApsState = {

    def updateEntityName(a: ApData): ApData = {
      ap.entityName match {
        case Some(x) => a.copy(entityName = ap.entityName)
        case None => a.copy()
      }
    }

    def updatePersonName(a: ApData) = {
      ap.personName match {
        case Some(x) => a.copy(personName = ap.personName)
        case None => a.copy()
      }
    }

    def updatePersonPosition(a: ApData) = {
      ap.personPosition match {
        case Some(x) => a.copy(personPosition = ap.personPosition)
        case None => a.copy()
      }
    }
    def updatePersonEmail(a: ApData) = {
      ap.personEmail match {
        case Some(x) => a.copy(personEmail = ap.personEmail)
        case None => a.copy()
      }
    }

    def updatePersonTel(a: ApData) = {
      ap.personTel match {
        case Some(x) => a.copy(personTel = ap.personTel)
        case None => a.copy()
      }
    }
    def updateCountry(a: ApData) = {
      ap.country match {
        case Some(x) => a.copy(country = ap.country)
        case None => a.copy()
      }
    }

    def updateOperationTypes(a: ApData) = {
      ap.operationTypes match {
        case Some(x) => a.copy(operationTypes = ap.operationTypes)
        case None => a.copy()
      }
    }

    def updateFinancing(a: ApData) = {
      ap.financing match {
        case Some(x) => a.copy(financing = ap.financing)
        case None => a.copy()
      }
    }

    def updateIsForLong(a: ApData) = {
      ap.isForLong match {
        case Some(x) => a.copy(isForLong = ap.isForLong)
        case None => a.copy()
      }
    }

    def updatePurpose(a: ApData) = {
      ap.purpose match {
        case Some(x) => a.copy(purpose = ap.purpose)
        case None => a.copy()
      }
    }

    def updateName(a: ApData): ApData = {
      ap.name match {
        case Some(x) => a.copy(name = ap.name)
        case None => a.copy()
      }
    }

    def updateCapital(a: ApData): ApData = {
      ap.capital match {
        case Some(x) => a.copy(capital = ap.capital)
        case None => a.copy()
      }
    }

    def updateApplicantInfo(a: ApData): ApData = {
      ap.applicantInfo match {
        case Some(x) => a.copy(applicantInfo = ap.applicantInfo)
        case None => a.copy()
      }
    }

    def updateAddress(a: ApData): ApData = {
      ap.address match {
        case Some(x) => a.copy(address = ap.address)
        case None => a.copy()
      }
    }

    def fillField = updateEntityName _ andThen updatePersonName andThen
      updatePersonPosition andThen updatePersonEmail andThen updatePersonTel andThen
      updateCountry andThen updateOperationTypes andThen
      updateFinancing andThen updateIsForLong andThen updatePurpose andThen updateName andThen
      updateCapital andThen updateApplicantInfo andThen updateAddress

    getApOpt(ap.id) match {
      case Some(x) =>
        val apCopy = x.copy(apData = fillField(x.apData))

        updateToStorage(apCopy)

      case None => this
    }

  }

  private def updateToStorage(apCopy: Ap): ApsState = {
    copy(
      storage.copy(
        aps = storage.aps + (apCopy.id -> apCopy)))
  }

  private def addFile(id: Ap.Id, file: ApFile): ApsState = {
    getApOpt(id) match {
      case Some(x) =>
        val apCopy = x.copy(apFiles = x.apFiles + (file.id -> file))

        updateToStorage(apCopy)

      case None => this
    }
  }

  private def updateFile(id: Ap.Id, file: ApFile): ApsState = {
    getApOpt(id) match {
      case Some(x) =>
        val apCopy = x.copy(apFiles = x.apFiles + (file.id -> file))

        updateToStorage(apCopy)

      case None => this
    }
  }

  private def removeFile(apId: Ap.Id, id: ApFile.Id): ApsState = {
    getApOpt(apId) match {
      case Some(x) =>
        val apCopy = x.copy(apFiles = x.apFiles - id)

        updateToStorage(apCopy)

      case None => this
    }
  }

  private def updateCriterion(id: Ap.Id, criterion: UpdateCriterion): ApsState = {

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

    getApOpt(id) match {
      case Some(x) =>
        val c: Criterion = x.criterions.get(criterion.id) match {
          case Some(y) =>
            y.copy(description = updatedD(y.description))
          case None =>
            Criterion(criterion.id, updatedD(ApString("", "")), Set.empty)
        }

        val apCopy = x.copy(criterions = x.criterions + (criterion.id -> c))

        updateToStorage(apCopy)

      case None => this
    }
  }

  private def toggleCriterion(apId: Ap.Id, id: Int): ApsState = {
    getApOpt(apId) match {
      case Some(x) =>
        x.criterions.get(id) match {
          case Some(y) =>
            val apCopy = x.copy(criterions = x.criterions +
              (id -> y.copy(isFinished = !y.isFinished)))

            updateToStorage(apCopy)

          case None => this

        }

      case None => this
    }
  }

  private def addCriterionFile(apId: Ap.Id, criterionId: Int, fileId: ApFile.Id): ApsState = {

    getApOpt(apId) match {
      case Some(x) =>
        x.criterions.get(criterionId) match {
          case Some(y) =>
            val apCopy = x.copy(criterions = x.criterions +
              (criterionId -> y.copy(attachment = y.attachment + fileId)))

            updateToStorage(apCopy)

          case None => this

        }

      case None => this
    }
  }

  private def removeCriterionFile(apId: Ap.Id, criterionId: Int, fileId: ApFile.Id): ApsState = {

    getApOpt(apId) match {
      case Some(x) =>
        x.criterions.get(criterionId) match {
          case Some(y) =>
            val apCopy = x.copy(criterions = x.criterions +
              (criterionId -> y.copy(attachment = y.attachment - fileId)))

            updateToStorage(apCopy)

          case None => this

        }

      case None => this
    }
  }

  private def cleanCriterion(apId: Ap.Id, id: Int): ApsState = {

    getApOpt(apId) match {
      case Some(x) =>
        x.criterions.get(id) match {
          case Some(y) =>
            val apCopy = x.copy(criterions = x.criterions - id)

            updateToStorage(apCopy)

          case None => this

        }

      case None => this
    }
  }

  private def changeStatus(id: Ap.Id, status: ApStatus): ApsState = {
    getApOpt(id) match {
      case Some(x) =>
        val apCopy = x.copy(apStatus = status, history = x.history :+ status)

        updateToStorage(apCopy)

      case None => this
    }
  }

  private def addExpert(apId: Ap.Id, userId: User.Id): ApsState = {
    getApOpt(apId) match {
      case Some(x) =>
        val bulletin: Bulletin = Bulletin(
          expertId = userId,
          date = ZonedDateTime.now(),
          criterions = Map.empty,
          finalResult = None,
          positiveConclusion = None,
          negativeConclusion = None,
          rejection = None,
          scores = None,
          isFinished = false)

        val expertise: Expertise = x.expertise.copy(
          experts = x.expertise.experts + userId,
          bulletins = x.expertise.bulletins + (userId -> bulletin))

        val apCopy = x.copy(expertise = expertise)

        updateToStorage(apCopy)

      case None => this
    }
  }

  private def removeExpert(apId: Ap.Id, userId: User.Id): ApsState = {
    getApOpt(apId) match {
      case Some(x) =>
        x.expertise.bulletins.get(userId) match {
          case Some(y) =>
            val bulletin: Bulletin = y
            val expertise: Expertise = x.expertise.copy(
              experts = x.expertise.experts - userId,
              bulletins = x.expertise.bulletins - bulletin.expertId)
            val apCopy = x.copy(expertise = expertise)

            updateToStorage(apCopy)
          case None => this
        }

      case None => this
    }
  }

  private def updateBulletin(apId: Ap.Id, bulletin: UpdateBulletin): ApsState = {

    def updateCriterions(b: Bulletin): Bulletin = {

      bulletin.criterions match {
        case Some(x) => b.copy(criterions = x)
        case None => b.copy()
      }
    }

    def updateFinalResult(b: Bulletin): Bulletin = {
      bulletin.finalResult match {
        case Some(x) => b.copy(finalResult = bulletin.finalResult)
        case None => b.copy()
      }
    }
    def updateScores(b: Bulletin): Bulletin = {
      bulletin.scores match {
        case Some(x) => b.copy(scores = bulletin.scores)
        case None => b.copy()
      }
    }

    def updatePositiveConclusion(b: Bulletin): Bulletin = {
      bulletin.positiveConclusion match {
        case Some(x) => b.copy(positiveConclusion = bulletin.positiveConclusion)
        case None => b.copy()
      }
    }

    def updateNegativeConclusion(b: Bulletin): Bulletin = {
      bulletin.negativeConclusion match {
        case Some(x) => b.copy(negativeConclusion = bulletin.negativeConclusion)
        case None => b.copy()
      }
    }

    def updateRejection(b: Bulletin): Bulletin = {
      bulletin.rejection match {
        case Some(x) => b.copy(rejection = bulletin.rejection)
        case None => b.copy()
      }
    }

    def updateIsFinished(b: Bulletin): Bulletin = {
      bulletin.isFinished match {
        case Some(x) => b.copy(isFinished = x)
        case None => b.copy()
      }
    }

    val update: (Bulletin) => Bulletin = updateCriterions _ andThen updateFinalResult andThen updateScores andThen
      updatePositiveConclusion andThen updateNegativeConclusion andThen updateRejection andThen updateIsFinished

    getApOpt(apId) match {
      case Some(x) =>
        x.expertise.bulletins.get(bulletin.expertId) match {
          case Some(y) =>
            val bulletinNew: Bulletin = update(y)
            val expertise: Expertise = x.expertise.copy(
              bulletins = x.expertise.bulletins + (bulletinNew.expertId -> bulletinNew))
            val apCopy = x.copy(expertise = expertise)

            updateToStorage(apCopy)

          case None => this
        }

      case None => this
    }
  }

  private def vote(apId: Ap.Id, userId: User.Id): ApsState = {
    getApOpt(apId) match {
      case Some(x) =>
        x.expertise.bulletins.get(userId) match {
          case Some(y) =>
            val bulletin: Bulletin = y.copy(isFinished = true)
            val expertise: Expertise = x.expertise.copy(
              bulletins = x.expertise.bulletins + (bulletin.expertId -> bulletin))

            val apCopy = x.copy(expertise = expertise)

            updateToStorage(apCopy)

          case None => this
        }

      case None => this
    }
  }

  private def changeManager(apId: Ap.Id, managerId: User.Id): ApsState = {
    getApOpt(apId) match {
      case Some(x) =>
        val apCopy = x.copy(projectManager = managerId)

        updateToStorage(apCopy)

      case None => this
    }
  }

  override def updated(event: PersistentEvent): ApsState = event match {
    case ApsActor.CreatedEvt(x) => create(x)
    case ApsActor.UpdatedEvt(x) => update(x)
    case ApsActor.DeletedEvt(x) => delete(x)

    case ApsActor.FillingFormEvt(x) => fillingForm(x)
    case ApsActor.AddFileEvt(id, file) => addFile(id, file)
    case ApsActor.UpdateFileEvt(id, file) => updateFile(id, file)
    case ApsActor.RemoveFileEvt(apId, id) => removeFile(apId, id)
    case ApsActor.UpdateCriterionEvt(id, criterion) =>
      updateCriterion(id, criterion)
    case ApsActor.FinishCriterionEvt(apId, id) => toggleCriterion(apId, id)
    case ApsActor.AddCriterionFileEvt(apId, criterionId, fileId) => addCriterionFile(apId, criterionId, fileId)
    case ApsActor.RemoveCriterionFileEvt(apId, criterionId, fileId) => removeCriterionFile(apId, criterionId, fileId)
    case ApsActor.CleanCriterionEvt(apId, criterionId) => cleanCriterion(apId, criterionId)
    case ApsActor.ChangeStatusEvt(id, s) => changeStatus(id, s)
    case ApsActor.AddExpertEvt(apId, userId) => addExpert(apId, userId)
    case ApsActor.RemoveExpertEvt(apId, userId) => removeExpert(apId, userId)
    case ApsActor.UpdateBulletinEvt(x, y) => updateBulletin(x, y)
    case ApsActor.VoteEvt(x, y) => vote(x, y)
    case ApsActor.ChangeManagerEvt(x, y) => changeManager(x, y)
  }

}

