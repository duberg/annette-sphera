

package annette.imc.serializer

import akka.serialization.SerializerWithStringManifest
import annette.imc.notification.actor.{ MailNotificationServiceState, SmsNotificationServiceState, SmsVerificationServiceState, _ }
import annette.imc.user.{ ImcUserActor, ImcUserState }
import annette.imc.{ ApsActor, ApsState }

/**
 * Created by valery on 10.04.17.
 */
class ImcSerializer extends SerializerWithStringManifest
  with ImcUserConverters
  with ApConverters
  with NotificationConverters {

  private val className = Option(this.getClass.getName).getOrElse("<undefined>")

  override def identifier: Int = 20170921

  override def toBinary(o: AnyRef): Array[Byte] = {
    o match {
      case obj: ImcUserActor.CreatedEvt => toImcUserCreatedEvtBinary(obj)
      case obj: ImcUserActor.UpdatedEvt => toImcUserUpdateEvtBinary(obj)
      case obj: ImcUserActor.DeletedEvt => toImcUserDeleteEvtBinary(obj)
      case obj: ImcUserState => toImcUserStateBinary(obj)

      case obj: ApsActor.CreatedEvt => toApCreatedEvtBinary(obj)
      case obj: ApsActor.UpdatedEvt => toApUpdateEvtBinary(obj)
      case obj: ApsActor.DeletedEvt => toApDeleteEvtBinary(obj)
      case obj: ApsState => toApStateBinary(obj)

      case obj: ApsActor.FillingFormEvt => toApFillingFormEvtBinary(obj)
      case obj: ApsActor.UpdateFileEvt => toApUpdateFileEvtBinary(obj)
      case obj: ApsActor.AddFileEvt => toApAddFileEvtBinary(obj)
      case obj: ApsActor.RemoveFileEvt => toApRemoveFileEvtBinary(obj)
      case obj: ApsActor.UpdateCriterionEvt => toApUpdateCriterionEvtBinary(obj)
      case obj: ApsActor.FinishCriterionEvt => toApFinishCriterionEvtBinary(obj)
      case obj: ApsActor.AddCriterionFileEvt => toApAddCriterionFileEvtBinary(obj)
      case obj: ApsActor.RemoveCriterionFileEvt => toApRemoveCriterionFileEvtBinary(obj)
      case obj: ApsActor.CleanCriterionEvt => toApCleanCriterionEvtBinary(obj)
      case obj: ApsActor.ChangeStatusEvt => toApChangeStatusEvtBinary(obj)
      case obj: ApsActor.AddExpertEvt => toApAddExpertEvtBinary(obj)
      case obj: ApsActor.RemoveExpertEvt => toApRemoveExpertEvtBinary(obj)
      case obj: ApsActor.UpdateBulletinEvt => toApUpdateBulletinEvtBinary(obj)
      case obj: ApsActor.VoteEvt => toApVoteEvtBinary(obj)
      case obj: ApsActor.ChangeManagerEvt => toApChangeManagerEvtBinary(obj)

      case obj: MailNotificationServiceActor.AddedNotificationEvt => toMailNotificationServiceAddedNotificationEvtBinary(obj)
      case obj: MailNotificationServiceActor.DeletedNotificationEvt => toMailNotificationServiceDeletedNotificationEvtBinary(obj)
      case obj: MailNotificationServiceActor.UpdatedRetryEvt => toMailNotificationServiceUpdatedRetryEvtBinary(obj)
      case obj: MailNotificationServiceState => toMailNotificationServiceStateBinary(obj)

      case obj: SmsNotificationServiceActor.AddedNotificationEvt => toSmsNotificationServiceAddedNotificationEvtBinary(obj)
      case obj: SmsNotificationServiceActor.DeletedNotificationEvt => toSmsNotificationServiceDeletedNotificationEvtBinary(obj)
      case obj: SmsNotificationServiceActor.UpdatedRetryEvt => toSmsNotificationServiceUpdatedRetryEvtBinary(obj)
      case obj: SmsNotificationServiceState => toSmsNotificationServiceStateBinary(obj)

      case obj: SmsVerificationServiceActor.AddedVerificationEvt => toSmsVerificationServiceAddedVerificationEvtBinary(obj)
      case obj: SmsVerificationServiceActor.DeletedVerificationEvt => toSmsVerificationServiceDeletedVerificationEvtBinary(obj)
      case obj: SmsVerificationServiceState => toSmsVerificationServiceStateBinary(obj)

      case _ =>
        val errorMsg = s"Can't serialize an object using $className [${o.toString}]"
        throw new IllegalArgumentException(errorMsg)
    }
  }

  override def manifest(o: AnyRef): String = {
    o match {
      case _: ImcUserActor.CreatedEvt => ImcUserCreatedEvtManifestV2
      case _: ImcUserActor.UpdatedEvt => ImcUserUpdatedEvtManifestV2
      case _: ImcUserActor.DeletedEvt => ImcUserDeletedEvtManifestV1
      case _: ImcUserState => ImcUserStateManifestV2

      case _: ApsActor.CreatedEvt => ApCreatedEvtManifestV5
      case _: ApsActor.UpdatedEvt => ApUpdatedEvtManifestV5
      case _: ApsActor.DeletedEvt => ApDeletedEvtManifestV1
      case _: ApsState => ApStateManifestV5

      case _: ApsActor.FillingFormEvt => ApFillingFormEvtManifestV1
      case _: ApsActor.UpdateFileEvt => ApUpdateFileEvtManifestV1
      case _: ApsActor.AddFileEvt => ApAddFileEvtManifestV1
      case _: ApsActor.RemoveFileEvt => ApRemoveFileEvtManifestV1
      case _: ApsActor.UpdateCriterionEvt => ApUpdateCriterionEvtManifestV1
      case _: ApsActor.FinishCriterionEvt => ApFinishCriterionEvtManifestV1
      case _: ApsActor.AddCriterionFileEvt => ApAddCriterionFileEvtManifestV1
      case _: ApsActor.RemoveCriterionFileEvt => ApRemoveCriterionFileEvtManifestV1
      case _: ApsActor.CleanCriterionEvt => ApCleanCriterionEvtManifestV1
      case _: ApsActor.ChangeStatusEvt => ApChangeStatusEvtManifestV1
      case _: ApsActor.AddExpertEvt => ApAddExpertEvtManifestV1
      case _: ApsActor.RemoveExpertEvt => ApRemoveExpertEvtManifestV1
      case _: ApsActor.UpdateBulletinEvt => ApUpdateBulletinEvtManifestV1
      case _: ApsActor.VoteEvt => ApVoteEvtManifestV1
      case _: ApsActor.ChangeManagerEvt => ApChangeManagerEvtManifestV1

      case _: MailNotificationServiceActor.AddedNotificationEvt => MailNotificationServiceAddedNotificationEvtManifestV1
      case _: MailNotificationServiceActor.DeletedNotificationEvt => MailNotificationServiceDeletedNotificationEvtManifestV1
      case _: MailNotificationServiceActor.UpdatedRetryEvt => MailNotificationServiceUpdatedRetryEvtManifestV1
      case _: MailNotificationServiceState => MailNotificationServiceStateManifestV1

      case _: SmsNotificationServiceActor.AddedNotificationEvt => SmsNotificationServiceAddedNotificationEvtManifestV1
      case _: SmsNotificationServiceActor.DeletedNotificationEvt => SmsNotificationServiceDeletedNotificationEvtManifestV1
      case _: SmsNotificationServiceActor.UpdatedRetryEvt => SmsNotificationServiceUpdatedRetryEvtManifestV1
      case _: SmsNotificationServiceState => SmsNotificationServiceStateManifestV1

      case _: SmsVerificationServiceActor.AddedVerificationEvt => SmsVerificationServiceAddedVerificationEvtManifestV1
      case _: SmsVerificationServiceActor.DeletedVerificationEvt => SmsVerificationServiceDeletedVerificationEvtManifestV1
      case _: SmsVerificationServiceState => SmsVerificationServiceStateManifestV1

      case _ =>
        val errorMsg = s"Can't create manifest for object using $className [${o.toString}]"
        throw new IllegalArgumentException(errorMsg)
    }
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    manifest match {
      case ImcUserCreatedEvtManifestV1 => fromImcUserCreatedEvtV1(bytes)
      case ImcUserCreatedEvtManifestV2 => fromImcUserCreatedEvtV2(bytes)
      case ImcUserUpdatedEvtManifestV1 => fromImcUserUpdatedEvtV1(bytes)
      case ImcUserUpdatedEvtManifestV2 => fromImcUserUpdatedEvtV2(bytes)
      case ImcUserDeletedEvtManifestV1 => fromImcUserDeletedEvtV1(bytes)
      case ImcUserStateManifestV1 => fromImcUserStateV1(bytes)
      case ImcUserStateManifestV2 => fromImcUserStateV2(bytes)

      case ApCreatedEvtManifestV1 => fromApCreatedEvtV1(bytes)
      case ApUpdatedEvtManifestV1 => fromApUpdatedEvtV1(bytes)
      case ApDeletedEvtManifestV1 => fromApDeletedEvtV1(bytes)
      case ApStateManifestV1 => fromApStateV1(bytes)

      case ApCreatedEvtManifestV3 => fromApCreatedEvtV3(bytes)
      case ApUpdatedEvtManifestV3 => fromApUpdatedEvtV3(bytes)
      case ApStateManifestV3 => fromApStateV3(bytes)

      case ApCreatedEvtManifestV4 => fromApCreatedEvtV4(bytes)
      case ApUpdatedEvtManifestV4 => fromApUpdatedEvtV4(bytes)
      case ApStateManifestV4 => fromApStateV4(bytes)

      case ApCreatedEvtManifestV5 => fromApCreatedEvtV5(bytes)
      case ApUpdatedEvtManifestV5 => fromApUpdatedEvtV5(bytes)
      case ApStateManifestV5 => fromApStateV5(bytes)

      case ApCreatedEvtManifestV2 => fromApCreatedEvtV2(bytes)
      case ApUpdatedEvtManifestV2 => fromApUpdatedEvtV2(bytes)
      case ApStateManifestV2 => fromApStateV2(bytes)

      case ApFillingFormEvtManifestV1 => fromApFillingFormEvtV1(bytes)
      case ApUpdateFileEvtManifestV1 => fromApUpdateFileEvtV1(bytes)
      case ApAddFileEvtManifestV1 => fromApAddFileEvtV1(bytes)
      case ApRemoveFileEvtManifestV1 => fromApRemoveFileEvtV1(bytes)
      case ApUpdateCriterionEvtManifestV1 => fromApUpdateCriterionEvtV1(bytes)
      case ApFinishCriterionEvtManifestV1 => fromApFinishCriterionEvtV1(bytes)
      case ApAddCriterionFileEvtManifestV1 => fromApAddCriterionFileEvtV1(bytes)
      case ApRemoveCriterionFileEvtManifestV1 => fromApRemoveCriterionFileEvtV1(bytes)
      case ApCleanCriterionEvtManifestV1 => fromApCleanCriterionEvtV1(bytes)
      case ApChangeStatusEvtManifestV1 => fromApChangeStatusEvtV1(bytes)
      case ApAddExpertEvtManifestV1 => fromApAddExpertEvtV1(bytes)
      case ApRemoveExpertEvtManifestV1 => fromApRemoveExpertEvtV1(bytes)
      case ApUpdateBulletinEvtManifestV1 => fromApUpdateBulletinEvtV1(bytes)
      case ApVoteEvtManifestV1 => fromApVoteEvtV1(bytes)
      case ApChangeManagerEvtManifestV1 => fromApChangeManagerEvtV1(bytes)

      case MailNotificationServiceAddedNotificationEvtManifestV1 => fromMailNotificationServiceAddedNotificationEvt(bytes)
      case MailNotificationServiceDeletedNotificationEvtManifestV1 => fromMailNotificationServiceDeletedNotificationEvt(bytes)
      case MailNotificationServiceUpdatedRetryEvtManifestV1 => fromMailNotificationServiceUpdatedRetryEvt(bytes)
      case MailNotificationServiceStateManifestV1 => fromMailNotificationServiceState(bytes)

      case SmsNotificationServiceAddedNotificationEvtManifestV1 => fromSmsNotificationServiceAddedNotificationEvt(bytes)
      case SmsNotificationServiceDeletedNotificationEvtManifestV1 => fromSmsNotificationServiceDeletedNotificationEvt(bytes)
      case SmsNotificationServiceUpdatedRetryEvtManifestV1 => fromSmsNotificationServiceUpdatedRetryEvt(bytes)
      case SmsNotificationServiceStateManifestV1 => fromSmsNotificationServiceState(bytes)

      case SmsVerificationServiceAddedVerificationEvtManifestV1 => fromSmsVerificationServiceAddedVerificationEvt(bytes)
      case SmsVerificationServiceDeletedVerificationEvtManifestV1 => fromSmsVerificationServiceDeletedVerificationEvt(bytes)
      case SmsVerificationServiceStateManifestV1 => fromSmsVerificationServiceState(bytes)

      case _ =>
        val errorMsg = s"Can't deserialize an object using $className manifest [$manifest]"
        throw new IllegalArgumentException(errorMsg)
    }
  }
}