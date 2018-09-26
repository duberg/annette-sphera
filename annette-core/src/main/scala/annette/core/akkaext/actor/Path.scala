package annette.core.akkaext.actor

import java.util.regex.Pattern

import annette.core.akkaext.actor.Path._
import annette.core.utils.Generator

trait PathLike[A <: PathLike[A]] extends Serializable { self: A =>
  lazy val name: String = fragments match {
    case _ :+ y :+ z if z contains ArrayElementSeparator => y + z
    case xs :+ x => x
  }

  lazy val raw: String = fragments.foldLeft("") {
    case (acc, fr) if fr contains ArrayElementSeparator => acc + fr
    case (acc, fr) if acc.isEmpty => fr
    case (acc, fr) => s"$acc$FragmentSeparatorA$fr"
  }

  lazy val parentOpt: Option[A] = fragments match {
    case x :+ y :+ z if z contains ArrayElementSeparator => Some(withFragments(x :+ y))
    case x :+ _ => Some(withFragments(x))
    case _ => None
  }

  lazy val parent: A = parentOpt.getOrElse(throw ParentPathException(fragments))

  def fragments: List[String]

  def head: String = fragments.head

  def tail: List[String] = fragments.tail

  def root: A = withFragments(fragments.head :: Nil)

  def isEmpty: Boolean = fragments.isEmpty

  def isArrayElement: Boolean = name contains ArrayElementSeparator

  def contains(fragment: String) = fragments contains fragment

  /**
   * Create a new child path.
   */
  def /(child: String): A = withFragments(fragments :+ child)
  def /(child: List[String]): A = withFragments(fragments ++ child)
  def /(child: Path): A = withFragments(fragments ++ child.fragments)
  def /(child: DataPath): A = withFragments(fragments ++ child.fragments)

  /**
   * The catamorphism for the Path data type.
   */
  def fold[T](
    ifPath: Path => T,
    ifDataPath: DataPath => T): T = this match {
    case x: Path => ifPath(x)
    case x: DataPath => ifDataPath(x)
  }

  def foldFragments(
    ifPath: PartialFunction[List[String], A],
    ifDataPath: PartialFunction[List[String], A]): A = this match {
    case x: Path => ifPath(x.fragments)
    case x: DataPath => ifDataPath(x.fragments)
  }

  def foldRoot[T](ifRoot: => T)(f: A => T): T = if (isEmpty) ifRoot else f(this)

  def withoutParent: A = parentOpt match {
    case Some(x) => withFragments(fragments.drop(x.fragments.size))
    case None => this
  }

  def withParent(path: A): A = withFragments(
    if (path.isEmpty) withoutParent.fragments
    else if (path.isArrayElement) path.fragments ::: withoutParent.fragments
    else path.fragments ::: withoutParent.fragments)

  def withFragments(fragments: List[String]): A
  def withSeparator: String = s"$raw$FragmentSeparatorA"
  def withArrayElementSeparator: String = s"$raw$ArrayElementSeparator"

  override def toString = raw

  def selectPath(relativePath: String) = PathSelection(this, relativePath)
  def >/(relativePath: String) = selectPath(relativePath)
  def >/(selection: SelectionPathFragment) = selectPath(selection.value)
}

object PathSelection {
  /**
   * Construct a PathSelection from the given string representing a path
   * relative to the given target.
   */
  def apply[A <: PathLike[A]](targetPath: A, relativePath: String): A = relativePath match {
    case SelectParent.value => targetPath.parent
    case SelectParentAsterisk.value => targetPath.fragments match {
      case ys :+ y1 :+ y2 => targetPath.withFragments(ys :+ Asterisk :+ y2)
      case ys :+ y1 => targetPath.withFragments(ys :+ Asterisk)
      case Nil => throw ParentPathException(targetPath.fragments)
    }
    case x => throw InvalidPathSelectionException(relativePath)
  }
}

abstract class SelectionPathFragment(val value: String)
case class SelectChildName(name: String) extends SelectionPathFragment(name)
case class SelectChildPattern(patternStr: String) extends SelectionPathFragment(patternStr)
case object SelectParent extends SelectionPathFragment("..")
case object SelectParentAsterisk extends SelectionPathFragment("../*")

trait NodePathMapper extends PathExtractor {
  def resolve(path: String, mappings: Map[String, ActorId]): Path = {
    val elements = fragments(path)
    if (elements.head contains "$") Path(mappings(elements.head), DataPath(elements.tail))
    else Path(mappings("$"), DataPath(elements))
  }
}

case class Path(nodeId: ActorId, dataPath: DataPath) extends PathLike[Path] {
  def fragments = {
    if (dataPath.isEmpty) List(nodeId.raw)
    else s"${nodeId.raw}$NodeIdFragmentSeparator" +: dataPath.fragments
  }
  def withFragments(fragments: List[String]) = apply(fragments)
}

object Path extends PathExtractor {
  val ArrayElementPattern = "^(\\[\\d+\\])$".r
  val ArrayIndexPattern = "^\\[(\\d+)\\]$".r
  val ArrayElementSeparator = "["
  val NodeIdFragmentPattern = "^(.+)@$".r
  val NodeIdFragmentSeparator = '@'
  val FragmentSeparatorA = "/"
  val FragmentSeparatorB = "."
  val Asterisk = "*"

  def apply(path: String): Path = path.split(s"$NodeIdFragmentSeparator/").toList match {
    case x :: xs => Path(ActorId(x), DataPath(xs.mkString))
    case Nil => throw InvalidPathException(List(path))
  }

  def apply(fragments: List[String]): Path = {
    fragments match {
      case NodeIdFragmentPattern(x) :: xs => Path(ActorId(x), DataPath(xs.mkString(FragmentSeparatorA)))
      case x :: xs => Path(ActorId(x), DataPath(xs.mkString(FragmentSeparatorA)))
      case Nil => throw InvalidPathException(fragments)
    }
  }

  def apply(nodeId: ActorId): Path = Path(nodeId, RootDataPath)
}

case class DataPath(fragments: List[String]) extends PathLike[DataPath] {
  def withFragments(fragments: List[String]) = copy(fragments = fragments)
}

object RootDataPath extends DataPath(List.empty)

trait PathExtractor {
  def fragments(path: String): List[String] = {
    val sep1 = Pattern.quote(FragmentSeparatorA)
    val sep2 = Pattern.quote(FragmentSeparatorB)
    val sep3 = Pattern.quote(ArrayElementSeparator)
    path.split(s"$sep1|$sep2|(?=$sep3)").toList
  }
}

object DataPath extends PathExtractor {
  def apply(path: String): DataPath = if (path.isEmpty) RootDataPath else DataPath(fragments(path))
}

case class InvalidPathException(fragments: List[String]) extends RuntimeException(s"Invalid path fragments: [$fragments]")
case class InvalidPathSelectionException(selection: String) extends RuntimeException(s"Invalid path selection: [$selection]")
case class ParentPathException(fragments: List[String]) extends RuntimeException(s"Parent path doesn't exist [$fragments]")

