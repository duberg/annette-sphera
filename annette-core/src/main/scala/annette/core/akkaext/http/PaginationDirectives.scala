package annette.core.akkaext.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive1, Rejection }
import com.typesafe.config.Config

import scala.collection.immutable.Seq
import scala.util.Try

/**
 * This is an example of url:
 *   /filter-test?page=1&size=10 or
 *   /filter-test?page=1&size=10&sort=name,asc;age,desc
 *   /filter-test?filter=status,1;gender,male&page=1&size=10&sort=name,asc;age,desc
 */
trait PaginationDirectives {

  private implicit class RichConfigOption[T](value: Option[T]) {
    def ||(defaultValue: T) = value.getOrElse(defaultValue)
  }

  val config: Config

  private def getOrFail[T](key: String)(implicit tag: scala.reflect.ClassTag[T]): T = tag.runtimeClass match {
    case clazz if clazz == classOf[String] => config.getString(key).asInstanceOf[T]
    case clazz if clazz == classOf[Char] => config.getString(key).toCharArray()(0).asInstanceOf[T]
    case clazz if clazz == classOf[Boolean] => config.getBoolean(key).asInstanceOf[T]
    case clazz if clazz == classOf[Int] => config.getInt(key).asInstanceOf[T]
    case clazz if clazz == classOf[Long] => config.getLong(key).asInstanceOf[T]
    case clazz => throw new RuntimeException(s"Invalid property type ${clazz.getSimpleName} for key $key")
  }

  private def get[T](key: String)(implicit tag: scala.reflect.ClassTag[T]): Option[T] = Try(getOrFail(key)).toOption

  private lazy val OffsetParam = get[String]("akka.http.extensions.pagination.offset-param-name") || "offset"
  private lazy val LimitParam = get[String]("akka.http.extensions.pagination.limit-param-name") || "limit"
  private lazy val SortParam = get[String]("akka.http.extensions.pagination.sort-param-name") || "sort"
  private lazy val FilterParam = get[String]("akka.http.extensions.pagination.filter-param-name") || "filter"

  private lazy val AscParam = get[String]("akka.http.extensions.pagination.asc-param-name") || "asc"
  private lazy val DescParam = get[String]("akka.http.extensions.pagination.desc-param-name") || "desc"

  private lazy val SortingSeparator = get[String]("akka.http.extensions.pagination.sorting-separator") || ";"
  private lazy val FilteringSeparator = get[String]("akka.http.extensions.pagination.filtering-separator") || ";"
  private lazy val OrderSeparator = get[Char]("akka.http.extensions.pagination.order-separator") || ','

  private lazy val ShouldFallbackToDefaults = get[Boolean]("akka.http.extensions.pagination.defaults.enabled") || false

  private lazy val ShouldAlwaysFallbackToDefaults = get[Boolean]("akka.http.extensions.pagination.defaults.always-fallback") || false

  private lazy val DefaultOffsetParam = get[Int]("akka.http.extensions.pagination.defaults.offset") || 0
  private lazy val DefaultLimitParam = get[Int]("akka.http.extensions.pagination.defaults.limit") || 10

  /**
   * Might return PageRequest
   * If both offset and limit are set - PageRequest is returned
   * If `defaults.enabled` -> if one of parameters is set, the other one is read from configuration
   * If `defaults.always-fallback` -> if none of the parameters is set, both are read from configuration
   * Otherwise returns None
   *
   * @return Option[PageRequest] - depending on configuration settings and HTTP request parameters
   */
  def optionalPagination: Directive1[Option[PageRequest]] =
    parameterMap.flatMap { params =>
      (params.get(OffsetParam).map(_.toInt), params.get(LimitParam).map(_.toInt)) match {
        case (Some(offset), Some(limit)) => provide(Some(deserializePage(offset, limit, params.get(SortParam), params.get(FilterParam))))
        case (Some(offset), None) if ShouldFallbackToDefaults => provide(Some(deserializePage(offset, DefaultLimitParam, params.get(SortParam), params.get(FilterParam))))
        case (Some(offset), None) => reject(MalformedPaginationRejection("Missing page limit parameter", None))
        case (None, Some(limit)) if ShouldFallbackToDefaults => provide(Some(deserializePage(DefaultOffsetParam, limit, params.get(SortParam), params.get(FilterParam))))
        case (None, Some(limit)) => reject(MalformedPaginationRejection("Missing page offset parameter", None))
        case (_, _) if ShouldAlwaysFallbackToDefaults => provide(Some(deserializePage(DefaultOffsetParam, DefaultLimitParam, params.get(SortParam), params.get(FilterParam))))
        case (_, _) => provide(None)
      }
    }

  /**
   * Always returns a PageRequest
   * If values are passed as part of HTTP request, they are taken from it
   * If not, (default) values are read from configuration
   *
   * @return PageRequest - taken from HTTP request or from configuration defaults
   */
  def pagination: Directive1[PageRequest] = {
    parameterMap.flatMap { params =>
      (params.get(OffsetParam).map(_.toInt), params.get(LimitParam).map(_.toInt)) match {
        case (Some(offset), Some(limit)) => provide(deserializePage(offset, limit, params.get(SortParam), params.get(FilterParam)))
        case (Some(offset), None) => provide(deserializePage(offset, DefaultLimitParam, params.get(SortParam), params.get(FilterParam)))
        case (None, Some(limit)) => provide(deserializePage(DefaultOffsetParam, limit, params.get(SortParam), params.get(FilterParam)))
        case (_, _) => provide(deserializePage(DefaultOffsetParam, DefaultLimitParam, params.get(SortParam), params.get(FilterParam)))
      }
    }
  }

  private def deserializePage(offset: Int, limit: Int, sorting: Option[String], filtering: Option[String]): PageRequest = {
    val sortingParam: Option[Map[String, Order]] =
      sorting
        .map(_.split(SortingSeparator)
          .map(_.span(_ != OrderSeparator))
          .collect {
            case (field, sort) if sort == ',' + AscParam => (field, Order.Asc)
            case (field, sort) if sort == ',' + DescParam => (field, Order.Desc)
          }.toMap)

    val filteringParam: Option[Map[String, String]] =
      filtering
        .map(_.split(FilteringSeparator)
          .map(_.span(_ != OrderSeparator)).toMap)
        .map(_.mapValues(_.tail))

    PageRequest(
      offset = offset,
      limit = limit,
      sort = sortingParam.getOrElse(Map.empty),
      filter = filteringParam.getOrElse(Map.empty))
  }

  case class MalformedPaginationRejection(errorMsg: String, cause: Option[Throwable] = None) extends Rejection

}

sealed trait Order

object Order {
  case object Asc extends Order
  case object Desc extends Order
}

case class PageRequest(
  offset: Int,
  limit: Int,
  sort: Map[String, Order],
  filter: Map[String, String])

case class PageResponse[T](elements: Seq[T], totalElements: Int)