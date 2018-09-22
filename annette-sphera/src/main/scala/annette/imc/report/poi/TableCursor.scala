package annette.imc.report.poi

import annette.imc.report.poi.Poi.RowIndex

class TableCursor(private var i: RowIndex) {
  def index: RowIndex = i
  def next(): Unit = i += 1
  def prev(): Unit = i -= 1
}

object TableCursor {
  def apply(i: RowIndex = 0): TableCursor = new TableCursor(i)
}