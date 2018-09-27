package annette.imc.serializer

import java.util.UUID

import annette.imc.notification.actor.{ MailNotificationServiceState, SmsNotificationServiceState, SmsVerificationServiceState, _ }
import annette.imc.notification.model._
import annette.imc.serializer.proto.notification._

import scala.concurrent.duration.Duration

trait NotificationConverters extends ApConverters {
  val MailNotificationServiceAddedNotificationEvtManifestV1 = "MailNotificationService.AddedNotificationEvt.v1"
  val MailNotificationServiceDeletedNotificationEvtManifestV1 = "MailNotificationService.DeletedNotificationEvt.v1"
  val MailNotificationServiceUpdatedRetryEvtManifestV1 = "MailNotificationService.UpdatedRetryEvt.v1"
  val MailNotificationServiceStateManifestV1 = "MailNotificationService.State.v1"

  val SmsNotificationServiceAddedNotificationEvtManifestV1 = "SmsNotificationService.AddedNotificationEvt.v1"
  val SmsNotificationServiceDeletedNotificationEvtManifestV1 = "SmsNotificationService.DeletedNotificationEvt.v1"
  val SmsNotificationServiceUpdatedRetryEvtManifestV1 = "SmsNotificationService.UpdatedRetryEvt.v1"
  val SmsNotificationServiceStateManifestV1 = "SmsNotificationService.State.v1"

  val SmsVerificationServiceAddedVerificationEvtManifestV1 = "SmsVerificationService.AddedVerificationEvt.v1"
  val SmsVerificationServiceDeletedVerificationEvtManifestV1 = "SmsVerificationService.DeletedVerificationEvt.v1"
  val SmsVerificationServiceStateManifestV1 = "SmsVerificationService.State.v1"

  def toMailNotificationServiceAddedNotificationEvtBinary(obj: MailNotificationServiceActor.AddedNotificationEvt): Array[Byte] =
    MailNotificationServiceAddedNotificationEvtV1(fromMailNotification(obj.x))
      .toByteArray

  def toSmsNotificationServiceAddedNotificationEvtBinary(obj: SmsNotificationServiceActor.AddedNotificationEvt): Array[Byte] =
    SmsNotificationServiceAddedNotificationEvtV1(fromSmsNotification(obj.x))
      .toByteArray

  def toSmsVerificationServiceAddedVerificationEvtBinary(obj: SmsVerificationServiceActor.AddedVerificationEvt): Array[Byte] =
    SmsVerificationServiceAddedVerificationEvtV1(fromSmsVerification(obj.x))
      .toByteArray

  def toMailNotificationServiceDeletedNotificationEvtBinary(obj: MailNotificationServiceActor.DeletedNotificationEvt): Array[Byte] =
    MailNotificationServiceDeletedNotificationEvtV1(obj.id.toString)
      .toByteArray

  def toSmsNotificationServiceDeletedNotificationEvtBinary(obj: SmsNotificationServiceActor.DeletedNotificationEvt): Array[Byte] =
    SmsNotificationServiceDeletedNotificationEvtV1(obj.id.toString)
      .toByteArray

  def toSmsVerificationServiceDeletedVerificationEvtBinary(obj: SmsVerificationServiceActor.DeletedVerificationEvt): Array[Byte] =
    SmsVerificationServiceDeletedVerificationEvtV1(obj.id.toString)
      .toByteArray

  def toMailNotificationServiceUpdatedRetryEvtBinary(obj: MailNotificationServiceActor.UpdatedRetryEvt): Array[Byte] =
    MailNotificationServiceUpdatedRetryEvtV1(obj.id.toString, obj.retry)
      .toByteArray

  def toSmsNotificationServiceUpdatedRetryEvtBinary(obj: SmsNotificationServiceActor.UpdatedRetryEvt): Array[Byte] =
    SmsNotificationServiceUpdatedRetryEvtV1(obj.id.toString, obj.retry)
      .toByteArray

  def toMailNotificationServiceStateBinary(obj: MailNotificationServiceState): Array[Byte] = {
    val n = obj.notifications.map { case (x, y) => x.toString -> fromMailNotification(y) }
    MailNotificationServiceStateV1(n).toByteArray
  }

  def toSmsNotificationServiceStateBinary(obj: SmsNotificationServiceState): Array[Byte] = {
    val n = obj.notifications.map { case (x, y) => x.toString -> fromSmsNotification(y) }
    SmsNotificationServiceStateV1(n).toByteArray
  }

  def toSmsVerificationServiceStateBinary(obj: SmsVerificationServiceState): Array[Byte] = {
    val n = obj.v.map { case (x, y) => x.toString -> fromSmsVerification(y) }
    SmsVerificationServiceStateV1(n).toByteArray
  }

  def fromMailNotificationServiceAddedNotificationEvt(bytes: Array[Byte]): MailNotificationServiceActor.AddedNotificationEvt = {
    val x = MailNotificationServiceAddedNotificationEvtV1.parseFrom(bytes).x
    MailNotificationServiceActor.AddedNotificationEvt(toMailNotification(x))
  }

  def toMailNotification(x: MailNotificationV1): MailNotification = {
    val opt1 = x.notificationOneof.opt1
    val opt2 = x.notificationOneof.opt2
    val opt3 = x.notificationOneof.opt3
    val opt4 = x.notificationOneof.opt4
    Seq(opt1, opt2, opt3, opt4).flatten.head match {
      case y: MailNotificationPasswordV1 => SendPasswordToEmail(
        id = UUID.fromString(y.id),
        email = y.email,
        password = y.password,
        language = y.language,
        templateParameters = y.templateParameters,
        retry = y.retry)
      case y: MailNotificationToExpertiseV1 => MailNotification.ToExpertise(
        id = UUID.fromString(y.id),
        email = y.email,
        language = y.language,
        templateParameters = y.templateParameters,
        retry = y.retry)
      case y: MailNotificationToReviewV1 => MailNotification.ToReview(
        id = UUID.fromString(y.id),
        email = y.email,
        language = y.language,
        templateParameters = y.templateParameters,
        retry = y.retry)
      case y: MailNotificationNotReadyV1 => MailNotification.ToReview(
        id = UUID.fromString(y.id),
        email = y.email,
        language = y.language,
        templateParameters = y.templateParameters,
        retry = y.retry)
      case _ => sys.error("Can't deserialize MailNotificationV1")
    }
  }

  def fromMailNotificationServiceDeletedNotificationEvt(bytes: Array[Byte]): MailNotificationServiceActor.DeletedNotificationEvt = {
    val x = UUID.fromString(MailNotificationServiceDeletedNotificationEvtV1.parseFrom(bytes).id)
    MailNotificationServiceActor.DeletedNotificationEvt(x)
  }

  def fromMailNotificationServiceUpdatedRetryEvt(bytes: Array[Byte]): MailNotificationServiceActor.UpdatedRetryEvt = {
    val x = MailNotificationServiceUpdatedRetryEvtV1.parseFrom(bytes)
    MailNotificationServiceActor.UpdatedRetryEvt(
      id = UUID.fromString(x.id),
      retry = x.retry)
  }

  def fromMailNotificationServiceState(bytes: Array[Byte]): MailNotificationServiceState = {
    val x = MailNotificationServiceStateV1.parseFrom(bytes).n.map {
      case (a, b) => (UUID.fromString(a), toMailNotification(b))
    }
    MailNotificationServiceState(x)
  }

  def fromSmsNotificationServiceAddedNotificationEvt(bytes: Array[Byte]): SmsNotificationServiceActor.AddedNotificationEvt = {
    val x = SmsNotificationServiceAddedNotificationEvtV1.parseFrom(bytes).x
    SmsNotificationServiceActor.AddedNotificationEvt(toSmsNotification(x))
  }

  def toSmsNotification(x: SmsNotificationV1): SmsNotification = {
    val opt1 = x.notificationOneof.opt1
    val opt2 = x.notificationOneof.opt2
    val opt3 = x.notificationOneof.opt3
    val opt4 = x.notificationOneof.opt4
    Seq(opt1, opt2, opt3, opt4).flatten.head match {
      case y: SmsNotificationPasswordV1 => SmsNotification.Password(
        id = UUID.fromString(y.id),
        phone = y.phone,
        password = y.password,
        language = y.language,
        retry = y.retry)
      case y: SmsNotificationVerificationV1 => SmsNotification.Verification(
        id = UUID.fromString(y.id),
        phone = y.phone,
        code = y.code,
        language = y.language,
        retry = y.retry)
      case y: SmsNotificationToExpertiseV1 => SmsNotification.ToExpertise(
        id = UUID.fromString(y.id),
        phone = y.phone,
        language = y.language,
        retry = y.retry)
      case y: SmsNotificationToReviewV1 => SmsNotification.ToReview(
        id = UUID.fromString(y.id),
        phone = y.phone,
        language = y.language,
        retry = y.retry)
      case _ => sys.error("Can't deserialize SmsNotificationV1")
    }
  }

  def fromSmsNotificationServiceDeletedNotificationEvt(bytes: Array[Byte]): SmsNotificationServiceActor.DeletedNotificationEvt = {
    val x = UUID.fromString(SmsNotificationServiceDeletedNotificationEvtV1.parseFrom(bytes).id)
    SmsNotificationServiceActor.DeletedNotificationEvt(x)
  }

  def fromSmsNotificationServiceUpdatedRetryEvt(bytes: Array[Byte]): SmsNotificationServiceActor.UpdatedRetryEvt = {
    val x = SmsNotificationServiceUpdatedRetryEvtV1.parseFrom(bytes)
    SmsNotificationServiceActor.UpdatedRetryEvt(
      id = UUID.fromString(x.id),
      retry = x.retry)
  }

  def fromSmsNotificationServiceState(bytes: Array[Byte]): SmsNotificationServiceState = {
    val x = SmsNotificationServiceStateV1.parseFrom(bytes).n.map {
      case (a, b) => (UUID.fromString(a), toSmsNotification(b))
    }
    SmsNotificationServiceState(x)
  }

  def fromSmsVerificationServiceAddedVerificationEvt(bytes: Array[Byte]): SmsVerificationServiceActor.AddedVerificationEvt = {
    val x = SmsVerificationServiceAddedVerificationEvtV1.parseFrom(bytes).x
    SmsVerificationServiceActor.AddedVerificationEvt(toSmsVerification(x))
  }

  def fromSmsVerificationServiceDeletedVerificationEvt(bytes: Array[Byte]): SmsVerificationServiceActor.DeletedVerificationEvt = {
    val x = UUID.fromString(SmsVerificationServiceDeletedVerificationEvtV1.parseFrom(bytes).id)
    SmsVerificationServiceActor.DeletedVerificationEvt(x)
  }

  def toSmsVerification(x: SmsVerificationV1): SmsVerification = {
    val opt1 = x.notificationOneof.opt1
    val opt2 = x.notificationOneof.opt2
    Seq(opt1, opt2).flatten.head match {
      case y: SmsVerificationStatusV1 => SmsVerification.Status(
        id = UUID.fromString(y.id),
        code = y.code,
        phone = y.phone,
        language = y.language,
        duration = Duration.fromNanos(y.duration))
      case y: SmsVerificationVotedV1 => SmsVerification.Voted(
        id = UUID.fromString(y.id),
        code = y.code,
        phone = y.phone,
        apId = UUID.fromString(y.apId),
        bulletin = toUpdateBulletin(y.bulletin),
        language = y.language,
        duration = Duration.fromNanos(y.duration))
      case _ => sys.error("Can't deserialize to SmsVerificationV1")
    }
  }

  def fromSmsVerificationServiceState(bytes: Array[Byte]): SmsVerificationServiceState = {
    val x = SmsVerificationServiceStateV1.parseFrom(bytes).n.map {
      case (a, b) => (UUID.fromString(a), toSmsVerification(b))
    }
    SmsVerificationServiceState(x)
  }

  def fromMailNotification(x: MailNotification): MailNotificationV1 = x match {
    case y: SendPasswordToEmail =>
      MailNotificationV1.defaultInstance.withOpt1(
        MailNotificationPasswordV1(
          id = y.id.toString,
          email = y.email,
          password = y.password,
          language = y.language,
          templateParameters = y.templateParameters,
          retry = y.retry))
    case y: MailNotification.ToExpertise =>
      MailNotificationV1.defaultInstance.withOpt2(
        MailNotificationToExpertiseV1(
          id = y.id.toString,
          email = y.email,
          language = y.language,
          templateParameters = y.templateParameters,
          retry = y.retry))
    case y: MailNotification.ToReview =>
      MailNotificationV1.defaultInstance.withOpt3(
        MailNotificationToReviewV1(
          id = y.id.toString,
          email = y.email,
          language = y.language,
          templateParameters = y.templateParameters,
          retry = y.retry))
    case y: MailNotification.NotReady =>
      MailNotificationV1.defaultInstance.withOpt4(
        MailNotificationNotReadyV1(
          id = y.id.toString,
          email = y.email,
          language = y.language,
          templateParameters = y.templateParameters,
          retry = y.retry))
  }

  def fromSmsNotification(x: SmsNotification): SmsNotificationV1 = x match {
    case SmsNotification.Password(id, phone, password, language, retry) =>
      SmsNotificationV1.defaultInstance.withOpt1(
        SmsNotificationPasswordV1(
          id = id.toString,
          phone = phone,
          password = password,
          language = language,
          retry = retry))
    case SmsNotification.Verification(id, phone, code, language, retry) =>
      SmsNotificationV1.defaultInstance.withOpt2(
        SmsNotificationVerificationV1(
          id = id.toString,
          phone = phone,
          code = code,
          language = language,
          retry = retry))
    case SmsNotification.ToExpertise(id, phone, language, retry) =>
      SmsNotificationV1.defaultInstance.withOpt3(
        SmsNotificationToExpertiseV1(
          id = id.toString,
          phone = phone,
          language = language,
          retry = retry))
    case SmsNotification.ToReview(id, phone, language, retry) =>
      SmsNotificationV1.defaultInstance.withOpt4(
        SmsNotificationToReviewV1(
          id = id.toString,
          phone = phone,
          language = language,
          retry = retry))
  }

  def fromSmsVerification(x: SmsVerification): SmsVerificationV1 = x match {
    case SmsVerification.Status(id, code, phone, language, duration) =>
      SmsVerificationV1.defaultInstance.withOpt1(
        SmsVerificationStatusV1(
          id = id.toString,
          code = code,
          phone = phone,
          language = language,
          duration = duration.toNanos))
    case SmsVerification.Voted(id, code, phone, apId, bulletin, language, duration) =>
      SmsVerificationV1.defaultInstance.withOpt2(
        SmsVerificationVotedV1(
          id = id.toString,
          code = code,
          phone = phone,
          apId = apId.toString,
          bulletin = fromUpdateBulletin(bulletin),
          language = language,
          duration = duration.toNanos))
  }
}