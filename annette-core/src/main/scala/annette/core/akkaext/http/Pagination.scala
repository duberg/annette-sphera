package annette.core.akkaext.http

import scala.reflect.ClassTag
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

object Pagination {
  def getObjFieldValue[T: TypeTag: ClassTag](field: String, obj: T): Any = {
    val rm = scala.reflect.runtime.currentMirror

    val accessors: Iterable[universe.MethodSymbol] = typeOf[T].members.collect {
      case m: MethodSymbol if m.isGetter && m.isPublic => m
    }

    val fieldSymbol = accessors
      .find(_.name.toString == field)
      .getOrElse(accessors.find(_.name.toString == "id").get)

    val instanceMirror = rm.reflect(obj)

    instanceMirror.reflectMethod(fieldSymbol).apply()
  }

  implicit object AnyOrdering extends Ordering[Any] {
    def compare(x: Any, y: Any): Int = (x, y) match {
      case (a: Int, b: Int) => Ordering.Int.compare(a, b)
      case (a: String, b: String) => Ordering.String.compare(a, b)
      case (a: Boolean, b: Boolean) => Ordering.Boolean.compare(a, b)
      case (a: BigDecimal, b: BigDecimal) => Ordering.BigDecimal.compare(a, b)
      case (a: Iterable[Any], b: Iterable[Any]) => Ordering.Iterable[Any](this).compare(a.map(_.toString), b.map(_.toString))
      case _ => Ordering.String.compare(x.toString, y.toString)
    }
  }

  def sort[T: TypeTag: ClassTag](items: List[T], fields: Map[String, Order]): List[T] = {
    (items /: fields) {
      case (_, (field, order)) =>
        order match {
          case Order.Asc => items.sortBy(getObjFieldValue(field, _))(AnyOrdering)
          case Order.Desc => items.sortBy(getObjFieldValue(field, _))(AnyOrdering.reverse)
        }
    }
  }

  def filter[T: TypeTag: ClassTag](items: List[T], filters: Map[String, String]): List[T] = {
    (items /: filters) {
      case (_, (field, value)) => items filter { item =>
        val fieldValue = getObjFieldValue(field, item)
        fieldValue match {
          case x: Int => value.toInt == x
          case x: Boolean => value.toBoolean == x
          case x => value == x
        }
      }
    }
  }

  /**
   * Paginates items
   *
   * @param items Items
   * @param page Page
   * @return Paginated items, total item count
   */
  def paginate[T: TypeTag: ClassTag](items: Iterable[T], page: PageRequest): (List[T], Int) = {
    val x = sort(filter(items.toList, page.filter), page.sort)
    x.slice(page.offset, page.offset + page.limit) -> x.size
  }

  def paginate[A, T: TypeTag: ClassTag](items: Map[A, T], page: PageRequest): (List[T], Int) = {
    val x = sort(filter(items.values.toList, page.filter), page.sort)
    x.slice(page.offset, page.offset + page.limit) -> x.size
  }
}
