package annette.imc.report.process

import annette.imc.report.model.TemplateDataC
import annette.imc.report.poi.Poi.{ Document, _ }

object TemplateProcessorC {
  def process(d: Document, x: TemplateDataC, p: Map[String, Any]): Unit = d << (x.parameters ++ p)
}
