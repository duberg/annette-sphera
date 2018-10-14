package annette.core.akkaext.http

import scala.reflect.ClassTag
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._
import shapeless._
import shapeless.ops.product._
import shapeless.syntax.std.product._

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

  // Evidence that an A is something that we can look around in for Qs that
  // satisfy some predicate.
  trait Searchable[A, Q] {
    def find(p: Q => Boolean)(a: A): Option[Q]
  }

  trait LowPrioritySearchable {
    implicit def hlistishSearchable[A, L <: HList, Q](
      implicit
      gen: Generic.Aux[A, L], s: Searchable[L, Q]): Searchable[A, Q] = new Searchable[A, Q] {
      def find(p: Q => Boolean)(a: A) = s.find(p)(gen to a)
    }
  }

  object Searchable extends LowPrioritySearchable {
    implicit def elemSearchable[A]: Searchable[A, A] = new Searchable[A, A] {
      def find(p: A => Boolean)(a: A) = if (p(a)) Some(a) else None
    }

    implicit def listSearchable[A, Q](implicit s: Searchable[A, Q]): Searchable[List[A], Q] =
      new Searchable[List[A], Q] {
        def find(p: Q => Boolean)(a: List[A]) = a.flatMap(s.find(p)).headOption
      }

    implicit def hnilSearchable[Q]: Searchable[HNil, Q] = new Searchable[HNil, Q] {
      def find(p: Q => Boolean)(a: HNil) = None
    }

    implicit def hlistSearchable[H, T <: HList, Q](
      implicit
      hs: Searchable[H, Q] = null, ts: Searchable[T, Q]): Searchable[H :: T, Q] = new Searchable[H :: T, Q] {
      def find(p: Q => Boolean)(a: H :: T) =
        Option(hs).flatMap(_.find(p)(a.head)) orElse ts.find(p)(a.tail)
    }
  }

  case class SearchableWrapper[A](a: A) {
    def deepFind[Q](p: Q => Boolean)(implicit s: Searchable[A, Q]) =
      s.find(p)(a)
  }

  implicit def wrapSearchable[A](a: A): SearchableWrapper[A] = SearchableWrapper(a)

  def sort[T: TypeTag: ClassTag](items: List[T], fields: Map[String, Order])(implicit s: Searchable[T, String]): List[T] = {
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
          // string search
          case x => x.toString.toLowerCase() contains value.toLowerCase()
        }
      }
    }
  }

  def search[T: TypeTag: ClassTag](items: List[T], search: Option[String])(implicit s: Searchable[T, String]): List[T] = {
    search.map(_.trim) match {
      case Some(x) if x.nonEmpty =>
        val z = items.filter(_.deepFind((zz: String) => zz.toString.toLowerCase contains x.toLowerCase).isDefined)
        println(items.map(_.deepFind((zz: String) => zz.toString.toLowerCase contains x.toLowerCase)))
        z
      case _ => items
    }
  }

  def slice[T: TypeTag: ClassTag](items: List[T], offset: Int, limit: Int): (List[T], Int) = items.slice(offset, offset + limit) -> items.size

  /**
   * Paginates items
   *
   * @param items Items
   * @param page Page
   * @return Paginated items, total item count
   */
  def paginate[T: TypeTag: ClassTag, L <: HList](items: Iterable[T], page: PageRequest)(implicit s: Searchable[T, String]) = {
    val x1 = filter(items.toList, page.filter)
    val x2 = search(x1, page.search)
    val x3 = sort(x2, page.sort)
    val x4 = slice(x3, page.offset, page.limit)
    x4
  }

  def paginate[A, T: TypeTag: ClassTag, L <: HList](items: Map[A, T], page: PageRequest)(implicit s: Searchable[T, String]) = {
    val x1 = filter(items.values.toList, page.filter)
    val x2 = search(x1, page.search)
    val x3 = sort(x2, page.sort)
    val x4 = slice(x3, page.offset, page.limit)
    x4
  }
}
