package annette.imc.report.poi

import org.apache.poi.xwpf.usermodel._

import scala.collection.JavaConverters._

/**
 * Apache POI обертка
 */
object Poi {
  type Document = XWPFDocument
  type Paragraph = XWPFParagraph
  type Table = XWPFTable
  type Row = XWPFTableRow
  type RowIndex = Int
  type RowData = Seq[String]
  type Cell = XWPFTableCell
  type Run = XWPFRun
  type Marker = String

  val MarkerStart = "{{"
  val MarkerEnd = "}}"

  /**
   * Документ - контейнер параграфов
   */
  implicit class PoiWordDocument(document: Document) {
    def head: Paragraph = paragraphs.head

    def paragraphs: Seq[Paragraph] =
      document.getParagraphs.asScala ++
        tables.flatMap(_.paragraphs)

    def text: String =
      document.getParagraphs.asScala
        .flatMap(_.getText)
        .mkString

    def firstTable: Table = tables.head

    def lastTable: Table = tables.last

    def tables: Seq[Table] = document.getTables.asScala

    def textRegions: Seq[TextRegion] =
      for {
        paragraph <- paragraphs
        run <- paragraph.runs
        (i, text) <- run.regions
      } yield TextRegion(run, i, text)

    def processed: Seq[TextRegion] = {
      textRegions
        .zipWithIndex
        .foreach {
          case (x, index) if x.text == MarkerStart =>
            x.run.replace("", x.index)
            val y = textRegions(index + 1)
            y.run.prepend(MarkerStart, y.index)
          case (x, index) if x.text == MarkerEnd =>
            x.run.replace("", x.index)
            val y = textRegions(index - 1)
            y.run.append(MarkerEnd, y.index)
          case (x, index) if x.text.reverse.take(2) == MarkerStart =>
            x.run.replace(x.text.dropRight(2), x.index)
            val y = textRegions(index + 1)
            y.run.prepend(MarkerStart, y.index)
          case (x, index) if x.text.take(2) == MarkerEnd =>
            x.run.replace(x.text.drop(2), x.index)
            val y = textRegions(index - 1)
            y.run.append(MarkerEnd, y.index)
          case _ =>
        }
      textRegions
    }

    def copy: Document = {
      val d = new Document()
      paragraphs.foreach(p => {
        val x = d.createParagraph()
        p.copyTo(x)
      })
      d
    }

    def replaceMarker(name: String, value: Any): Unit =
      processed.foreach {
        case TextRegion(r, i, t) if t contains name.toMarker =>
          r.replace(t, name.toMarker, value, i)
        case _ =>
      }

    /**
     * Заполнение документа параметрами
     *
     * @param x parameters
     */
    def fill(x: Map[String, Any]): Unit =
      x.foreach((replaceMarker _).tupled)

    /**
     * Заполнение документа параметрами
     *
     * @param x parameters
     */
    def <<(x: Map[String, Any]): Unit = fill(x)
  }

  /**
   * Параграф документа
   */
  implicit class PoiParagraph(paragraph: Paragraph) {
    def runs: Seq[Run] = paragraph.getRuns.asScala

    def copyTo(p: Paragraph): Paragraph = {
      val y = p.createRun()
      runs.foreach(x => {
        y.setText(x.getText(0))
        y.setFontSize(x.getFontSize)
        y.setFontFamily(x.getFontFamily)
        y.setBold(x.isBold)
        y.setItalic(x.isItalic)
        y.setColor(x.getColor)
      })
      p
    }

    def delete(): Unit = {
      val d = paragraph.getDocument
      val x = d.getPosOfParagraph(paragraph)
      d.removeBodyElement(x)
    }
  }

  /**
   * Таблица - контейнер записей
   */
  implicit class PoiTable(table: Table) {
    private var cursor: TableCursor = TableCursor()

    def apply(i: RowIndex): Row = table getRow i

    /**
     * Обновить запись с индексом
     */
    def update(i: RowIndex, x: RowData): Table = {
      val r = table(i)
      (0 /: x)((i, y) => {
        r.getCell(i).setText(y)
        i + 1
      })
      table
    }

    def head: Row = table(0)

    def rows: Seq[Row] = table.getRows.asScala

    def cells: Seq[Cell] = rows.flatMap(_.getTableCells.asScala)

    def paragraphs: Seq[Paragraph] = cells.flatMap(_.getParagraphs.asScala)

    def runs: Seq[Run] = paragraphs.flatMap(_.getRuns.asScala)

    def addRow(x: RowData): Table = {
      val r = table.insertNewTableRow(cursor.index)
      cursor.next()
      x.foreach(x => {
        val c = r.createCell()
        c.setText(x)
      })
      table
    }

    def addRows(x: Seq[RowData]): Table = {
      x foreach { y =>
        val r = table.insertNewTableRow(cursor.index)
        cursor.next()
        (0 /: y)((i, z) => {
          val c = r.createCell()
          c.setText(z)
          i + 1
        })
      }
      table
    }

    /**
     * Добавить запись в текущую позицию курсора
     */
    def +=(x: RowData): Table = addRow(x)

    /**
     * Добавить записи в текущую позицию курсора
     */
    def ++=(x: Seq[RowData]): Table = addRows(x)

    def style: String = table.getStyleID

    def withCursor(x: TableCursor): PoiTable = {
      cursor = x
      this
    }
  }

  implicit class PoiCell(cell: Cell) {
    def text: String = cell.getText
    def textOpt = Option(text)
  }

  /**
   * Run - контейнер строк
   */
  implicit class PoiRun(run: Run) {
    def apply(x: Int): String = run getText x

    def head: String = run(0)

    def regions: Map[Int, String] =
      run.getCTR.getTArray.indices
        .map(i => i -> run.getText(i))
        .toMap

    def document: Document = run.getDocument

    def replace(text: String, marker: String, value: Any, region: Int): Unit = {
      val v = Option(value match {
        case x: Iterable[String] @unchecked => text.replace(marker, toText(x))
        case x => text.replace(marker, x.toString)
      })
      v.foreach(run.setText(_, region))
    }

    def replace(value: Any, region: Int): Unit =
      run.setText(value.toString, region)

    def prepend(value: Any, region: Int): Unit =
      run.setText(s"${value.toString}${run.text}", region)

    def append(value: Any, region: Int): Unit =
      run.setText(s"${run.text}${value.toString}", region)
  }

  implicit class RichText(text: String) {
    def searchMarker: Option[String] = {
      val x = text.indexOf(MarkerStart) != -1
      val y = text.indexOf(MarkerEnd) != -1
      (x, y) match {
        case (true, _) => Option(MarkerStart)
        case (_, true) => Option(MarkerEnd)
        case _ => None
      }
    }
    def toMarker: Marker = s"$MarkerStart$text$MarkerEnd"
  }

  implicit class RichIterable(iterable: Iterable[Any]) {
    def asNumberedList: NumberedList = NumberedList(iterable)
  }

  case class TextRegion(run: Run, index: Int, text: String)

  def toText(x: Iterable[String]): String = ("" /: x)((a, b) => if (a == "") b else s"$a, $b")
}
