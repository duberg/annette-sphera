package annette.imc.report.process

import annette.imc.report.model.TemplateDataB
import annette.imc.report.poi.Poi.{ Document, _ }
import annette.imc.report.poi.TableCursor
import shapeless.syntax.std.tuple._

object TemplateProcessorB {
  def process(d: Document, x: TemplateDataB, p: Map[String, Any]): Unit = {
    val t1 = d.firstTable
    val y = x.documentRows.map(Seq.apply(_))
    (0 /: y)((i, r) => {
      if (i == 0) t1(0) = r
      else t1 += r
      i + 1
    })
    val c = TableCursor(1)
    val t2 = d.lastTable.withCursor(c)
    t2 ++= x.commentRows.map(_.toList)
    d << (x.parameters ++ p)
  }
}
