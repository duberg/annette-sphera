package annette.core.serializer

import java.time.ZonedDateTime
import java.util.UUID

import scala.concurrent.duration.{ Duration, FiniteDuration }

object Implicits {
  implicit def longToFiniteDuration(x: Long): FiniteDuration = Duration.fromNanos(x)

  implicit def finiteDurationToLong(x: FiniteDuration): Long = x.toNanos

  implicit def stringToZonedDateTime(x: String): ZonedDateTime = ZonedDateTime.parse(x)

  //  implicit def stringOptToZonedDateTimeOpt(x: Option[String]): Option[ZonedDateTime] =
  //    typeAToOptionTypeB(x)
  //
  //  implicit def longOptToFiniteDurationOpt(x: Option[Long])(implicit convert: Long => FiniteDuration): Option[FiniteDuration] =
  //    typeAToOptionTypeB(x)
  //
  //  implicit def finiteDurationOptToLongOpt(x: Option[FiniteDuration])(implicit convert: FiniteDuration => Long): Option[Long] =
  //    typeAToOptionTypeB(x)
  //
  //implicit def zonedDateTimeToString(x: ZonedDateTime): String = x.toString
  //
  //  implicit def zonedDateTimeOptToStringOpt(x: Option[ZonedDateTime]): Option[String] =
  //    typeAToOptionTypeB(x)

  implicit def stringToUUID(x: String): UUID = UUID.fromString(x)
  //implicit def stringOptToUUIDOpt(x: Option[String]): Option[UUID] = typeAToOptionTypeB(x)
  //implicit def UUIDOptToStringOpt(x: Option[UUID]): Option[String] = typeAToOptionTypeB(x)
  implicit def UUIDToString(x: UUID): String = x.toString
  //implicit def UUIDSetToStringSeq(x: Set[UUID]): Seq[String] = x.map(UUIDToString)
  //implicit def stringSeqToUUIDSet(x: Seq[String]): Set[UUID] = toSet(x)
  implicit def MapStringValueToMapUUIDValue(x: Map[String, String]): Map[String, UUID] = x.mapValues(stringToUUID)
  implicit def MapUUIDValueToMapStringValue(x: Map[String, UUID]): Map[String, String] = x.mapValues(_.toString)

  implicit def stringToBigDecimal(x: String): BigDecimal = BigDecimal(x)

  /**
   * = Option conversions =
   */

  /**
   * Оборачивание в опцию применяется в сущностях UpdateEntity при десириализации
   */
  //  implicit def ValueToOpt[T](x: T): Option[T] = x match {
  //    case None => None
  //    case Some(y: T) => Some(y)
  //    case y => Some(y)
  //  }

  /**
   * Оборачивание в опцию применяется в сущностях UpdateEntity при сириализации
   */
  //  implicit def OptToValue[T](x: Option[Option[T]]): Option[T] = x.flatten
  //
  //  implicit def OptToSet[T](x: Option[Set[T]]): Set[T] = x.getOrElse(Set.empty)
  //  implicit def OptSetToSeq[T](x: Option[Set[T]]): Seq[T] = x.map(_.toSeq).getOrElse(Seq.empty)
  //  implicit def OptSeqToSeq[T](x: Option[Seq[T]]): Seq[T] = x.getOrElse(Seq.empty)
  //  //implicit def OptToMap[A, B](x: Option[Map[A, B]]): Map[A, B] = x.getOrElse(Map.empty)
  //  implicit def SetToOptSeq[T](x: Set[T]): Option[Seq[T]] = Some(x.toSeq)
  //  implicit def SeqToOptSet[T](x: Seq[T]): Option[Set[T]] = Some(x.toSet)
  //
  //  implicit def toSeq[A, B](x: Set[A])(implicit convert: A => B): Seq[B] = x.map(convert).toSeq
  //  implicit def toSet[A, B](x: Seq[A])(implicit convert: A => B): Set[B] = x.map(convert).toSet
  //  implicit def toSet[A, B](x: Set[A])(implicit convert: A => B): Set[B] = x map convert

  /**
   * = Map conversions =
   */
  //  implicit def mapConversion1[A, B, C](x: Map[C, A])(implicit convert: A => B): Map[C, B] =
  //    x mapValues convert
  //
  //  implicit def mapValueOpt[A, B, C](x: Map[C, A])(implicit convert: A => B): Option[Map[C, B]] =
  //    if (x.isEmpty) None else Some(mapConversion1(x))
  //
  //  implicit def optMapValue[A, B, C](x: Option[Map[C, A]])(implicit convert: A => B): Map[C, B] =
  //    x.fold(Map[C, B]())(mapConversion1(_))
  //
  //  def typeAToOptionTypeB[A, B](x: Option[A])(implicit convert: A => B): Option[B] =
  //    x map convert
}
