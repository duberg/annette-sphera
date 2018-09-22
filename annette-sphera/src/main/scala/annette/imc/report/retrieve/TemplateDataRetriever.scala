package annette.imc.report.retrieve

import annette.core.domain.tenancy.model.User
import annette.imc.model.Ap
import annette.imc.report.model.Templates._
import annette.imc.report.model.{ Report, TemplateData }

import scala.concurrent.Future

trait TemplateDataRetriever extends TemplateDataRetrieverA
  with TemplateDataRetrieverB
  with TemplateDataRetrieverC
  with TemplateDataRetrieverD
  with TemplateDataRetrieverE {
  def retrieve(id: Report.Id, apId: Ap.Id, expertId: User.Id, language: String): Future[TemplateData] =
    id match {
      case `predefinedIdA` => retrieveDataA(apId, language)
      case `predefinedIdB` => retrieveDataB(apId, language)
      case `predefinedIdC` => retrieveDataC(apId, expertId, language)
      case `predefinedIdD` => retrieveDataD(apId, expertId, language)
      case `predefinedIdE` => retrieveDataE(apId, language)
    }
}
