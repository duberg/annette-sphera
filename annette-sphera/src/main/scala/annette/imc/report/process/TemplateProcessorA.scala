package annette.imc.report.process

import annette.imc.report.model.TemplateDataA
import annette.imc.report.poi.Poi.{ Document, _ }
import annette.imc.report.poi.TableCursor
import shapeless.syntax.std.tuple._

object TemplateProcessorA {
  def process(d: Document, x: TemplateDataA, p: Map[String, Any]): Unit = {
    val c = TableCursor(1)
    val t = d.firstTable.withCursor(c)
    t ++= x.presentRows.map(_.toList)
    c.next()
    t ++= x.membersOfTheExpertCouncilRows.map(_.toList)
    c.next()
    t ++= x.inviteesRows.map(_.toList)
    d << (x.parameters ++ p)
  }
}
