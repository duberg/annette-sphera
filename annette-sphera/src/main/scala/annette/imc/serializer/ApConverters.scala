
package annette.imc.serializer

import java.time.ZonedDateTime
import java.util.UUID

import annette.core.domain.tenancy.model.User
import annette.imc.model.Ap.ApFiles
import annette.imc.model.ApFile.{ FileType, Id }
import annette.imc.model._
import annette.imc.serializer.proto.ap._
import annette.imc.{ ApsActor, ApsState, ApsStorage }

trait ApConverters {

  val ApCreatedEvtManifestV1 = "Ap.CreatedEvt.v1"
  val ApCreatedEvtManifestV2 = "Ap.CreatedEvt.v2"
  val ApCreatedEvtManifestV3 = "Ap.CreatedEvt.v3"
  val ApCreatedEvtManifestV4 = "Ap.CreatedEvt.v4"
  val ApCreatedEvtManifestV5 = "Ap.CreatedEvt.v5"
  val ApUpdatedEvtManifestV1 = "Ap.UpdatedEvt.v1"
  val ApUpdatedEvtManifestV2 = "Ap.UpdatedEvt.v2"
  val ApUpdatedEvtManifestV3 = "Ap.UpdatedEvt.v3"
  val ApUpdatedEvtManifestV4 = "Ap.UpdatedEvt.v4"
  val ApUpdatedEvtManifestV5 = "Ap.UpdatedEvt.v5"
  val ApDeletedEvtManifestV1 = "Ap.DeletedEvt.v1"
  val ApStateManifestV1 = "Ap.State.v1"
  val ApStateManifestV2 = "Ap.State.v2"
  val ApStateManifestV3 = "Ap.State.v3"
  val ApStateManifestV4 = "Ap.State.v4"
  val ApStateManifestV5 = "Ap.State.v5"

  val ApFillingFormEvtManifestV1 = "Ap.FillingFormEvt.v1"
  val ApUpdateFileEvtManifestV1 = "Ap.UpdateFileEvt.v1"
  val ApAddFileEvtManifestV1 = "Ap.AddFileEvt.v1"
  val ApRemoveFileEvtManifestV1 = "Ap.RemoveFileEvt.v1"
  val ApUpdateCriterionEvtManifestV1 = "Ap.UpdateCriterionEvt.v1"
  val ApFinishCriterionEvtManifestV1 = "Ap.FinishCriterionEvt.v1"
  val ApAddCriterionFileEvtManifestV1 = "Ap.AddCriterionFileEvt.v1"
  val ApRemoveCriterionFileEvtManifestV1 = "Ap.RemoveCriterionFileEvt.v1"
  val ApCleanCriterionEvtManifestV1 = "Ap.CleanCriterionEvt.v1"
  val ApChangeStatusEvtManifestV1 = "Ap.ChangeStatusEvt.v1"
  val ApAddExpertEvtManifestV1 = "Ap.AddExpertEvt.v1"
  val ApRemoveExpertEvtManifestV1 = "Ap.RemoveExpertEvt.v1"
  val ApUpdateBulletinEvtManifestV1 = "Ap.UpdateBulletinEvt.v1"
  val ApVoteEvtManifestV1 = "Ap.VoteEvt.v1"
  val ApChangeManagerEvtManifestV1 = "Ap.ChangeManagerEvt.v1"

  def toApCreatedEvtBinary(obj: ApsActor.CreatedEvt): Array[Byte] = {
    ApCreatedEvtV5(fromAp(obj.ap))
      .toByteArray
  }

  def toApUpdateEvtBinary(obj: ApsActor.UpdatedEvt): Array[Byte] = {
    ApUpdatedEvtV5(fromAp(obj.ap))
      .toByteArray
  }

  def toApDeleteEvtBinary(obj: ApsActor.DeletedEvt): Array[Byte] = {
    ApDeletedEvtV1(obj.ap.toString)
      .toByteArray
  }

  def toApStateBinary(obj: ApsState): Array[Byte] = {
    val storage: Map[String, ApV5] = obj.storage.aps.map {
      case (id, ap) =>
        val d: ApV5 = fromAp(ap)
        id.toString -> d
    }
    ApStateV5(storage)
      .toByteArray
  }

  def toApFillingFormEvtBinary(obj: ApsActor.FillingFormEvt): Array[Byte] = {
    ApFillingFormEvtV1(fromUpdateAp(obj.update)).toByteArray
  }

  def toApUpdateFileEvtBinary(obj: ApsActor.UpdateFileEvt): Array[Byte] = {
    ApUpdateFileEvtV1(obj.apId.toString, fromApFile(obj.file)).toByteArray
  }

  def toApAddFileEvtBinary(obj: ApsActor.AddFileEvt): Array[Byte] = {
    ApAddFileEvtV1(obj.ap.toString, fromApFile(obj.file)).toByteArray
  }

  def toApRemoveFileEvtBinary(obj: ApsActor.RemoveFileEvt): Array[Byte] = {
    ApRemoveFileEvtV1(obj.ap.toString, obj.fileId.toString).toByteArray
  }

  def toApAddCriterionFileEvtBinary(obj: ApsActor.AddCriterionFileEvt): Array[Byte] = {
    ApAddCriterionFileEvtV1(obj.ap.toString, obj.criterionId, obj.fileId.toString).toByteArray
  }

  def toApUpdateCriterionEvtBinary(obj: ApsActor.UpdateCriterionEvt): Array[Byte] = {
    ApUpdateCriterionEvtV1(obj.ap.toString, fromUpdateCritrion(obj.criterion)).toByteArray
  }

  def toApFinishCriterionEvtBinary(obj: ApsActor.FinishCriterionEvt): Array[Byte] = {
    ApFinishCriterionEvtV1(obj.ap.toString, obj.criterionId).toByteArray
  }

  def toApRemoveCriterionFileEvtBinary(obj: ApsActor.RemoveCriterionFileEvt): Array[Byte] = {
    ApRemoveCriterionFileEvtV1(obj.ap.toString, obj.criterionId, obj.fileId.toString).toByteArray
  }

  def toApCleanCriterionEvtBinary(obj: ApsActor.CleanCriterionEvt): Array[Byte] = {
    ApCleanCriterionEvtV1(obj.ap.toString, obj.criterionId).toByteArray
  }

  def toApChangeStatusEvtBinary(obj: ApsActor.ChangeStatusEvt): Array[Byte] = {
    ApChangeStatusEvtV1(obj.ap.toString, fromApStatus(obj.status)).toByteArray
  }

  def toApAddExpertEvtBinary(obj: ApsActor.AddExpertEvt): Array[Byte] = {
    ApAddExpertEvtV1(obj.ap.toString, obj.userId.toString).toByteArray
  }

  def toApRemoveExpertEvtBinary(obj: ApsActor.RemoveExpertEvt): Array[Byte] = {
    ApRemoveExpertEvtV1(obj.ap.toString, obj.userId.toString).toByteArray
  }

  def toApUpdateBulletinEvtBinary(obj: ApsActor.UpdateBulletinEvt): Array[Byte] = {
    ApUpdateBulletinEvtV1(obj.ap.toString, fromUpdateBulletin(obj.bulletin)).toByteArray
  }

  def toApVoteEvtBinary(obj: ApsActor.VoteEvt): Array[Byte] = {
    ApVoteEvtV1(obj.ap.toString, obj.expert.toString).toByteArray
  }

  def toApChangeManagerEvtBinary(obj: ApsActor.ChangeManagerEvt): Array[Byte] = {
    ApChangeManagerEvtV1(obj.ap.toString, obj.manager.toString).toByteArray
  }

  def fromApCreatedEvtV1(bytes: Array[Byte]): ApsActor.CreatedEvt = {
    val d = ApCreatedEvtV1.parseFrom(bytes).ap
    ApsActor.CreatedEvt(toAp(d))
  }

  def fromApCreatedEvtV2(bytes: Array[Byte]): ApsActor.CreatedEvt = {
    val d = ApCreatedEvtV2.parseFrom(bytes).ap
    ApsActor.CreatedEvt(toAp(d))
  }

  def fromApCreatedEvtV3(bytes: Array[Byte]): ApsActor.CreatedEvt = {
    val d = ApCreatedEvtV3.parseFrom(bytes).ap
    ApsActor.CreatedEvt(toAp(d))
  }

  def fromApCreatedEvtV4(bytes: Array[Byte]): ApsActor.CreatedEvt = {
    val d = ApCreatedEvtV4.parseFrom(bytes).ap
    ApsActor.CreatedEvt(toAp(d))
  }

  def fromApCreatedEvtV5(bytes: Array[Byte]): ApsActor.CreatedEvt = {
    val d = ApCreatedEvtV5.parseFrom(bytes).ap
    ApsActor.CreatedEvt(toAp(d))
  }

  def fromApUpdatedEvtV1(bytes: Array[Byte]): ApsActor.UpdatedEvt = {
    val d = ApUpdatedEvtV1.parseFrom(bytes).ap
    ApsActor.UpdatedEvt(toAp(d))
  }

  def fromApUpdatedEvtV2(bytes: Array[Byte]): ApsActor.UpdatedEvt = {
    val d = ApUpdatedEvtV2.parseFrom(bytes).ap
    ApsActor.UpdatedEvt(toAp(d))
  }

  def fromApUpdatedEvtV4(bytes: Array[Byte]): ApsActor.UpdatedEvt = {
    val d = ApUpdatedEvtV4.parseFrom(bytes).ap
    ApsActor.UpdatedEvt(toAp(d))
  }

  def fromApUpdatedEvtV3(bytes: Array[Byte]): ApsActor.UpdatedEvt = {
    val d = ApUpdatedEvtV3.parseFrom(bytes).ap
    ApsActor.UpdatedEvt(toAp(d))
  }

  def fromApUpdatedEvtV5(bytes: Array[Byte]): ApsActor.UpdatedEvt = {
    val d = ApUpdatedEvtV5.parseFrom(bytes).ap
    ApsActor.UpdatedEvt(toAp(d))
  }

  def fromApDeletedEvtV1(bytes: Array[Byte]): ApsActor.DeletedEvt = {
    val id = UUID.fromString(ApDeletedEvtV1.parseFrom(bytes).id)
    ApsActor.DeletedEvt(id)
  }

  def fromApFillingFormEvtV1(bytes: Array[Byte]): ApsActor.FillingFormEvt = {
    val upd: UpdateApV1 = ApFillingFormEvtV1.parseFrom(bytes).up
    ApsActor.FillingFormEvt(toUpdateAp(upd))
  }

  def fromApUpdateFileEvtV1(bytes: Array[Byte]): ApsActor.UpdateFileEvt = {
    val ev = ApUpdateFileEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.apId)
    val file = toApFile(ev.file)
    ApsActor.UpdateFileEvt(id, file)
  }

  def fromApAddFileEvtV1(bytes: Array[Byte]): ApsActor.AddFileEvt = {
    val ev = ApAddFileEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val file = toApFile(ev.file)
    ApsActor.AddFileEvt(id, file)
  }

  def fromApRemoveFileEvtV1(bytes: Array[Byte]): ApsActor.RemoveFileEvt = {
    val ev = ApRemoveFileEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val file = UUID.fromString(ev.fileId)
    ApsActor.RemoveFileEvt(id, file)
  }

  def fromApUpdateCriterionEvtV1(bytes: Array[Byte]): ApsActor.UpdateCriterionEvt = {
    val ev = ApUpdateCriterionEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val c = toUpdateCriterion(ev.criterion)
    ApsActor.UpdateCriterionEvt(id, c)
  }

  def fromApFinishCriterionEvtV1(bytes: Array[Byte]): ApsActor.FinishCriterionEvt = {
    val ev = ApFinishCriterionEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    ApsActor.FinishCriterionEvt(id, ev.criterionId)
  }

  def fromApAddCriterionFileEvtV1(bytes: Array[Byte]): ApsActor.AddCriterionFileEvt = {
    val ev = ApAddCriterionFileEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val fileId = UUID.fromString(ev.fileId)
    ApsActor.AddCriterionFileEvt(id, ev.criterionId, fileId)
  }

  def fromApRemoveCriterionFileEvtV1(bytes: Array[Byte]): ApsActor.RemoveCriterionFileEvt = {
    val ev = ApRemoveCriterionFileEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val fileId = UUID.fromString(ev.fileId)
    ApsActor.RemoveCriterionFileEvt(id, ev.criterionId, fileId)
  }

  def fromApCleanCriterionEvtV1(bytes: Array[Byte]): ApsActor.CleanCriterionEvt = {
    val ev = ApCleanCriterionEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    ApsActor.CleanCriterionEvt(id, ev.criterionId)
  }

  def fromApChangeStatusEvtV1(bytes: Array[Byte]): ApsActor.ChangeStatusEvt = {
    val ev = ApChangeStatusEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val status = toApStatus(ev.status)
    ApsActor.ChangeStatusEvt(id, status)
  }

  def fromApAddExpertEvtV1(bytes: Array[Byte]): ApsActor.AddExpertEvt = {
    val ev = ApAddExpertEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val user = UUID.fromString(ev.userId)
    ApsActor.AddExpertEvt(id, user)
  }

  def fromApRemoveExpertEvtV1(bytes: Array[Byte]): ApsActor.RemoveExpertEvt = {
    val ev = ApRemoveExpertEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val user = UUID.fromString(ev.userId)
    ApsActor.RemoveExpertEvt(id, user)
  }

  def fromApUpdateBulletinEvtV1(bytes: Array[Byte]): ApsActor.UpdateBulletinEvt = {
    val ev = ApUpdateBulletinEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val bulletin = toUpdateBulletin(ev.bulletin)
    ApsActor.UpdateBulletinEvt(id, bulletin)
  }

  def fromApVoteEvtV1(bytes: Array[Byte]): ApsActor.VoteEvt = {
    val ev = ApVoteEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val expert = UUID.fromString(ev.expert)
    ApsActor.VoteEvt(id, expert)
  }

  def fromApChangeManagerEvtV1(bytes: Array[Byte]): ApsActor.ChangeManagerEvt = {
    val ev = ApChangeManagerEvtV1.parseFrom(bytes)
    val id = UUID.fromString(ev.ap)
    val manager = UUID.fromString(ev.manager)
    ApsActor.ChangeManagerEvt(id, manager)
  }

  def fromApStateV1(bytes: Array[Byte]): ApsState = {
    val storage = ApStateV1.parseFrom(bytes).storage
      .map {
        case (id, d) =>
          UUID.fromString(id) -> toAp(d)
      }
    ApsState(ApsStorage(storage))
  }

  def fromApStateV2(bytes: Array[Byte]): ApsState = {
    val storage = ApStateV2.parseFrom(bytes).storage
      .map {
        case (id, d) =>
          UUID.fromString(id) -> toAp(d)
      }
    ApsState(ApsStorage(storage))
  }

  def fromApStateV3(bytes: Array[Byte]): ApsState = {
    val storage = ApStateV3.parseFrom(bytes).storage
      .map {
        case (id, d) =>
          UUID.fromString(id) -> toAp(d)
      }
    ApsState(ApsStorage(storage))
  }

  def fromApStateV4(bytes: Array[Byte]): ApsState = {
    val storage = ApStateV4.parseFrom(bytes).storage
      .map {
        case (id, d) =>
          UUID.fromString(id) -> toAp(d)
      }
    ApsState(ApsStorage(storage))
  }

  def fromApStateV5(bytes: Array[Byte]): ApsState = {
    val storage = ApStateV5.parseFrom(bytes).storage
      .map {
        case (id, d) =>
          UUID.fromString(id) -> toAp(d)
      }
    ApsState(ApsStorage(storage))
  }

  private def toApString(d: ApStringV1): ApString = ApString(ru = d.ru, en = d.en)
  private def toOptionApString(d: Option[ApStringV1]): Option[ApString] = d.map(toApString)

  private def toOperationType(l: Seq[OperationTypeV1]): Option[Set[OperationType]] = {
    val set = l.map(o => OperationType(o.nameMessage, toOptionApString(o.details))).toSet
    if (set.isEmpty) None
    else Some(set)
  }

  private def toApData(d: ApDataV1): ApData = {
    ApData(
      entityName = toOptionApString(d.entityName),
      personName = toOptionApString(d.personName),
      personPosition = toOptionApString(d.personPosition),
      personEmail = d.personEmail,
      personTel = d.personTel,
      country = d.country,
      operationTypes = toOperationType(d.operationTypes).flatMap(_.head.details),
      financing = d.financing.map(x => Set(Financing(x))),
      isForLong = d.isForLong,
      purpose = d.purpose)
  }
  private def toApData(d: ApDataV2): ApData = {
    ApData(
      entityName = toOptionApString(d.entityName),
      personName = toOptionApString(d.personName),
      personPosition = toOptionApString(d.personPosition),
      personEmail = d.personEmail,
      personTel = d.personTel,
      country = d.country,
      operationTypes = toOperationType(d.operationTypes).flatMap(_.head.details),
      financing = Option(d.financing.toSet.map(x => Financing(x))),
      isForLong = d.isForLong,
      purpose = d.purpose,
      name = toOptionApString(d.name),
      capital = d.capital,
      applicantInfo = toOptionApString(d.applicantInfo),
      address = d.address)
  }

  private def toApData(d: ApDataV3): ApData = {
    ApData(
      entityName = toOptionApString(d.entityName),
      personName = toOptionApString(d.personName),
      personPosition = toOptionApString(d.personPosition),
      personEmail = d.personEmail,
      personTel = d.personTel,
      country = d.country,
      operationTypes = toOptionApString(d.operationTypes),
      financing = Option(d.financing.toSet.map(x => Financing(x))),
      isForLong = d.isForLong,
      purpose = d.purpose,
      name = toOptionApString(d.name),
      capital = d.capital,
      applicantInfo = toOptionApString(d.applicantInfo),
      address = d.address)
  }

  def toApStatus(d: ApStatusV1): ApStatus = {
    ApStatus(
      nameMessage = d.nameMessage,
      changeTime = ZonedDateTime.parse(d.changeTime),
      userId = d.userId.map(UUID.fromString),
      comment = d.comment,
      result = toOptionApString(d.resultDecision).map(ApResult))
  }

  private def toApStatusSeq(l: Seq[ApStatusV1]): Seq[ApStatus] = l.map(toApStatus)

  private def toApFile(f: ApFileV1): ApFile = {
    ApFile(
      id = UUID.fromString(f.id),
      name = f.name,
      fileType = FileType(f.fileType),
      lang = f.lang,
      comments = f.comments)
  }

  private def toApFiles(m: Map[String, ApFileV1]): Ap.ApFiles = m.map { case (id, f) => UUID.fromString(id) -> toApFile(f) }

  private def toCriterion(d: CriterionV1): Criterion = {
    Criterion(
      id = d.id,
      description = toApString(d.description),
      attachment = d.attachment.map(UUID.fromString).toSet,
      isFinished = d.isFinished)
  }

  private def toUpdateCriterion(d: UpdateCriterionV1): UpdateCriterion = {
    UpdateCriterion(
      id = d.id,
      descriptionRu = d.descriptionRu,
      descriptionEn = d.descriptionEn)
  }

  private def toCriterionsMap(m: Map[Int, CriterionV1]): Map[Int, Criterion] = m.map { case (n, cr) => n -> toCriterion(cr) }

  private def toScores(s: ScoresV1): Scores = {
    Scores(
      s.medical, s.educational, s.scientific)
  }

  private def toBulletin(d: BulletinV1): Bulletin = {
    Bulletin(
      expertId = UUID.fromString(d.expertId),
      date = ZonedDateTime.parse(d.date),
      criterions = Map.empty,
      finalResult = None,
      positiveConclusion = None,
      negativeConclusion = None,
      rejection = d.rejection.map {
        case ApStringV1(ru, en) =>
          if (ru.trim.length > 0 || en.trim.length > 0) true
          else false
      },
      scores = d.scores.map(toScores),
      isFinished = d.isFinished)
  }

  private def toBulletin(d: BulletinV2): Bulletin = {
    Bulletin(
      expertId = UUID.fromString(d.expertId),
      date = ZonedDateTime.parse(d.date),
      criterions = d.criterions.map(x => x._1 -> Vote(x._2)),
      finalResult = d.finalResult,
      positiveConclusion = d.positiveConclusion,
      negativeConclusion = d.negativeConclusion,
      rejection = d.rejection,
      scores = d.scores.map(toScores),
      isFinished = d.isFinished)

  }

  private def toBulletin(d: BulletinV3): Bulletin = {
    Bulletin(
      expertId = UUID.fromString(d.expertId),
      date = ZonedDateTime.parse(d.date),
      criterions = d.criterions.map(x => x._1 -> Vote(x._2.decision, x._2.pluses, x._2.minuses)),
      finalResult = d.finalResult,
      positiveConclusion = d.positiveConclusion,
      negativeConclusion = d.negativeConclusion,
      rejection = d.rejection,
      scores = d.scores.map(toScores),
      isFinished = d.isFinished)

  }

  def toUpdateBulletin(d: UpdateBulletinV1): UpdateBulletin = {
    UpdateBulletin(
      expertId = UUID.fromString(d.expertId),
      criterions = if (d.criterions.isEmpty) None else Option(d.criterions.map(x => x._1 -> Vote(x._2))),
      finalResult = d.finalResult,
      positiveConclusion = d.positiveConclusion,
      negativeConclusion = d.negativeConclusion,
      rejection = d.rejection,
      scores = d.scores.map(toScores),
      isFinished = d.isFinished)
  }

  private def toBulletinsMapV1(m: Map[String, BulletinV1]): Map[User.Id, Bulletin] = m.map { case (s, b) => UUID.fromString(s) -> toBulletin(b) }

  private def toBulletinsMapV2(m: Map[String, BulletinV2]): Map[User.Id, Bulletin] = m.map { case (s, b) => UUID.fromString(s) -> toBulletin(b) }

  private def toBulletinsMapV3(m: Map[String, BulletinV3]): Map[User.Id, Bulletin] = m.map { case (s, b) => UUID.fromString(s) -> toBulletin(b) }

  private def toExpertise(d: ExpertiseV1): Expertise = {
    Expertise(
      experts = d.experts.map(UUID.fromString).toSet,
      bulletins = toBulletinsMapV1(d.bulletins))
  }

  private def toExpertise(d: ExpertiseV2): Expertise = {
    Expertise(
      experts = d.experts.map(UUID.fromString).toSet,
      bulletins = toBulletinsMapV2(d.bulletins))
  }

  private def toExpertise(d: ExpertiseV3): Expertise = {
    Expertise(
      experts = d.experts.map(UUID.fromString).toSet,
      bulletins = toBulletinsMapV3(d.bulletins))
  }

  private def toAp(d: ApV1): Ap =
    Ap(
      id = UUID.fromString(d.id),
      apData = toApData(d.apData),
      apStatus = toApStatus(d.apStatus),
      history = toApStatusSeq(d.history),
      projectManager = UUID.fromString("00000000-0000-0000-0000-000000000000"),
      apFiles = toApFiles(d.apFiles),
      criterions = toCriterionsMap(d.criterions),
      expertise = toExpertise(d.expertise))

  private def toAp(d: ApV2): Ap =
    Ap(
      id = UUID.fromString(d.id),
      apData = toApData(d.apData),
      apStatus = toApStatus(d.apStatus),
      history = toApStatusSeq(d.history),
      projectManager = UUID.fromString("00000000-0000-0000-0000-000000000000"),
      apFiles = toApFiles(d.apFiles),
      criterions = toCriterionsMap(d.criterions),
      expertise = toExpertise(d.expertise))

  private def toAp(d: ApV3): Ap =
    Ap(
      id = UUID.fromString(d.id),
      apData = toApData(d.apData),
      apStatus = toApStatus(d.apStatus),
      history = toApStatusSeq(d.history),
      projectManager = UUID.fromString("00000000-0000-0000-0000-000000000000"),
      apFiles = toApFiles(d.apFiles),
      criterions = toCriterionsMap(d.criterions),
      expertise = toExpertise(d.expertise))

  private def toAp(d: ApV4): Ap =
    Ap(
      id = UUID.fromString(d.id),
      apData = toApData(d.apData),
      apStatus = toApStatus(d.apStatus),
      history = toApStatusSeq(d.history),
      projectManager = UUID.fromString("00000000-0000-0000-0000-000000000000"),
      apFiles = toApFiles(d.apFiles),
      criterions = toCriterionsMap(d.criterions),
      expertise = toExpertise(d.expertise))
  private def toAp(d: ApV5): Ap =
    Ap(
      id = UUID.fromString(d.id),
      apData = toApData(d.apData),
      apStatus = toApStatus(d.apStatus),
      history = toApStatusSeq(d.history),
      projectManager = UUID.fromString(d.projectManager),
      apFiles = toApFiles(d.apFiles),
      criterions = toCriterionsMap(d.criterions),
      expertise = toExpertise(d.expertise))

  private def toUpdateAp(d: UpdateApV1): UpdateAp = {
    UpdateAp(
      id = UUID.fromString(d.id),
      entityName = toOptionApString(d.entityName),
      personName = toOptionApString(d.personName),
      personPosition = toOptionApString(d.personPosition),
      personEmail = d.personEmail,
      personTel = d.personTel,
      country = d.country,
      operationTypes = toOptionApString(d.operationTypes),
      financing = Option(d.financing.toSet.map(x => Financing(x))),
      isForLong = d.isForLong,
      purpose = d.purpose,
      name = toOptionApString(d.name),
      capital = d.capital,
      applicantInfo = toOptionApString(d.applicantInfo),
      address = d.address)
  }

  private def toApStringV1(d: ApString): ApStringV1 = ApStringV1(d.ru, d.en)
  private def toOptionApStringV1(d: Option[ApString]): Option[ApStringV1] = d.map(toApStringV1)

  private def fromOperationType(d: OperationType): OperationTypeV1 = {
    OperationTypeV1(
      nameMessage = d.nameMessage,
      details = toOptionApStringV1(d.details))
  }

  private def fromOperationTypes(d: Option[Set[OperationType]]): Seq[OperationTypeV1] = d.map(_.map(fromOperationType).toSeq).getOrElse(Seq.empty)

  //  private def fromApData(d: ApData): ApDataV1 = {
  //    ApDataV1(
  //      entityName = toOptionApStringV1(d.entityName),
  //      personName = toOptionApStringV1(d.personName),
  //      personPosition = toOptionApStringV1(d.personPosition),
  //      personEmail = d.personEmail,
  //      personTel = d.personTel,
  //      country = d.country,
  //      operationTypes = fromOperationTypes(d.operationTypes),
  //      participation = None,
  //      financing = d.financing.map(_.head.nameMessage),
  //      isForLong = d.isForLong,
  //      purpose = d.purpose)
  //  }

  private def fromApData(d: ApData): ApDataV3 = {
    ApDataV3(
      entityName = toOptionApStringV1(d.entityName),
      personName = toOptionApStringV1(d.personName),
      personPosition = toOptionApStringV1(d.personPosition),
      personEmail = d.personEmail,
      personTel = d.personTel,
      country = d.country,
      operationTypes = toOptionApStringV1(d.operationTypes),
      financing = d.financing match {
        case None => Seq.empty
        case Some(x) => x.map(_.nameMessage).toSeq
      },
      isForLong = d.isForLong,
      purpose = d.purpose,
      name = toOptionApStringV1(d.name),
      capital = d.capital,
      applicantInfo = toOptionApStringV1(d.applicantInfo),
      address = d.address)
  }

  def fromApStatus(d: ApStatus): ApStatusV1 = {
    ApStatusV1(
      d.nameMessage,
      d.changeTime.toString,
      d.userId.map(_.toString),
      d.comment,
      toOptionApStringV1(d.result.map(_.decision)))
  }

  private def fromApStatusSeq(s: Seq[ApStatus]): Seq[ApStatusV1] = s.map(fromApStatus)

  private def fromApFile(f: ApFile): ApFileV1 = {
    ApFileV1(
      id = f.id.toString,
      name = f.name,
      lang = f.lang,
      fileType = f.fileType.name,
      comments = f.comments)
  }

  private def fromApFiles(m: ApFiles): Map[String, ApFileV1] = m.map { case (id, f) => id.toString -> fromApFile(f) }

  private def fromCritrion(d: Criterion): CriterionV1 = {
    CriterionV1(
      id = d.id,
      description = toApStringV1(d.description),
      attachment = d.attachment.map(_.toString).toSeq,
      isFinished = d.isFinished)
  }

  private def fromCriterionsMap(m: Map[Int, Criterion]): Map[Int, CriterionV1] = m.map { case (n, cr) => n -> fromCritrion(cr) }

  private def fromVote(d: Vote): VoteV2 = {
    VoteV2(
      decision = d.decision,
      pluses = d.pluses,
      minuses = d.minuses)
  }

  private def fromVoteMap(m: Map[Int, Vote]): Map[Int, VoteV2] = m.map { case (n, v) => n -> fromVote(v) }

  private def fromScores(d: Scores): ScoresV1 = {
    ScoresV1(
      medical = d.medical,
      educational = d.educational,
      scientific = d.scientific)
  }

  //  private def fromBulletin(d: Bulletin): BulletinV2 = {
  //    BulletinV2(
  //      expertId = d.expertId.toString,
  //      date = d.date.toString,
  //      criterions = d.criterions.map(x => x._1 -> x._2.decision),
  //      pluses = None,
  //      minuses = None,
  //      finalResult = d.finalResult,
  //      positiveConclusion = d.positiveConclusion,
  //      negativeConclusion = d.negativeConclusion,
  //      rejection = d.rejection,
  //      scores = d.scores.map(fromScores),
  //      isFinished = d.isFinished)
  //  }

  private def fromBulletin(d: Bulletin): BulletinV3 = {
    BulletinV3(
      expertId = d.expertId.toString,
      date = d.date.toString,
      criterions = fromVoteMap(d.criterions),
      finalResult = d.finalResult,
      positiveConclusion = d.positiveConclusion,
      negativeConclusion = d.negativeConclusion,
      rejection = d.rejection,
      scores = d.scores.map(fromScores),
      isFinished = d.isFinished)
  }

  def fromUpdateBulletin(d: UpdateBulletin): UpdateBulletinV1 = {
    UpdateBulletinV1(
      expertId = d.expertId.toString,
      criterions = d.criterions.getOrElse(Map.empty).map(x => x._1 -> x._2.decision),
      pluses = None,
      minuses = None,
      finalResult = d.finalResult,
      positiveConclusion = d.positiveConclusion,
      negativeConclusion = d.negativeConclusion,
      rejection = d.rejection,
      scores = d.scores.map(fromScores),
      isFinished = d.isFinished)
  }

  //  private def fromBulletinMap(m: Map[Id, Bulletin]): Map[String, BulletinV2] = m.map { case (id, b) => id.toString -> fromBulletin(b) }
  private def fromBulletinMap(m: Map[Id, Bulletin]): Map[String, BulletinV3] = m.map { case (id, b) => id.toString -> fromBulletin(b) }

  //  private def fromExpertise(d: Expertise): ExpertiseV2 = {
  //    ExpertiseV2(
  //      d.experts.map(_.toString).toSeq,
  //      fromBulletinMap(d.bulletins))
  //  }
  private def fromExpertise(d: Expertise): ExpertiseV3 = {
    ExpertiseV3(
      d.experts.map(_.toString).toSeq,
      fromBulletinMap(d.bulletins))
  }

  //  private def fromAp(d: Ap): ApV2 = {
  //    ApV2(
  //      d.id.toString,
  //      fromApData(d.apData),
  //      fromApStatus(d.apStatus),
  //      fromApStatusSeq(d.history),
  //      fromApFiles(d.apFiles),
  //      fromCriterionsMap(d.criterions),
  //      fromExpertise(d.expertise))
  //  }

  private def fromAp(d: Ap): ApV5 = {
    ApV5(
      d.id.toString,
      fromApData(d.apData),
      fromApStatus(d.apStatus),
      fromApStatusSeq(d.history),
      d.projectManager.toString,
      fromApFiles(d.apFiles),
      fromCriterionsMap(d.criterions),
      fromExpertise(d.expertise))
  }

  private def fromUpdateAp(d: UpdateAp): UpdateApV1 = {
    UpdateApV1(
      id = d.id.toString,
      entityName = toOptionApStringV1(d.entityName),
      personName = toOptionApStringV1(d.personName),
      personPosition = toOptionApStringV1(d.personPosition),
      personEmail = d.personEmail,
      personTel = d.personTel,
      country = d.country,
      operationTypes = toOptionApStringV1(d.operationTypes),
      financing = d.financing match {
        case None => Seq.empty
        case Some(x) => x.map(_.nameMessage).toSeq
      },
      isForLong = d.isForLong,
      purpose = d.purpose,
      name = toOptionApStringV1(d.name),
      capital = d.capital,
      applicantInfo = toOptionApStringV1(d.applicantInfo),
      address = d.address)

  }

  private def fromUpdateCritrion(d: UpdateCriterion): UpdateCriterionV1 = {
    UpdateCriterionV1(
      id = d.id,
      descriptionRu = d.descriptionRu,
      descriptionEn = d.descriptionEn)
  }

}
