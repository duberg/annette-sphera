
package annette.core.utils

sealed abstract class FilterOption[+A]
final case class Value[+A](x: A) extends FilterOption[A]
final case object AnyValue extends FilterOption[Nothing]
final case object NoValue extends FilterOption[Nothing]
