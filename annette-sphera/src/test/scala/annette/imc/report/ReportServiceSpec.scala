//package annette.imc.report
//
//import akka.actor.ActorSystem
//import akka.testkit.TestKit
//import annette.core.test.PersistenceSpec
//import annette.imc.report.model.{ReportFormatType, Templates}
//
//class ReportServiceSpec extends TestKit(ActorSystem("ReportServiceSpec"))
//  with PersistenceSpec
//  with NewReportService {
//  override def beforeAll() {
//    //initUsersDao()
//  }
//  "A ReportServiceSpec" when {
//    "predefinedA" must {
//      "generate word" in {
//        for {
//          (apId, a) <- newReportService
//          x <- a.generateWithTemplateData(
//            Templates.predefinedIdA,
//            apId,
//            "target/reportA.docx",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Word,
//            templateDataA,
//            "RU"
//          )
//        } yield succeed
//      }
//      "generate pdf" in {
//        for {
//          (apId, a) <- newReportService
//          x <- a.generateWithTemplateData(
//            Templates.predefinedIdA,
//            apId,
//            "target/reportA.pdf",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Pdf,
//            templateDataA,
//            "RU"
//          )
//        } yield succeed
//      }
//    }
//    "predefinedB" must {
//      "generate word" in {
//        for {
//          (apId, a) <- newReportService
//          x <- a.generateWithTemplateData(
//            Templates.predefinedIdB,
//            apId,
//            "target/reportB.docx",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Word,
//            templateDataB,
//            "RU"
//          )
//        } yield succeed
//      }
//      "generate pdf" in {
//        for {
//          (apId, a) <- newReportService
//          x <- a.generateWithTemplateData(
//            Templates.predefinedIdB,
//            apId,
//            "target/reportB.pdf",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Pdf,
//            templateDataB,
//            "RU"
//          )
//        } yield succeed
//      }
//    }
//    "predefinedC" must {
//      "generate word" in {
//        for {
//          (apId, a) <- newReportService
//          x <- a.generateWithTemplateData(
//            Templates.predefinedIdC,
//            apId,
//            "target/reportRuC.docx",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Word,
//            templateDataC,
//            "RU"
//          )
//          y <- a.generateWithTemplateData(
//            Templates.predefinedIdC,
//            apId,
//            "target/reportEnC.docx",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Word,
//            templateDataC,
//            "EN"
//          )
//        } yield succeed
//      }
//      "generate pdf" in {
//        for {
//          (apId, a) <- newReportService
//          x <- a.generateWithTemplateData(
//            Templates.predefinedIdC,
//            apId,
//            "target/reportRuC.pdf",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Pdf,
//            templateDataC,
//            "RU",
//          )
//          y <- a.generateWithTemplateData(
//            Templates.predefinedIdC,
//            apId,
//            "target/reportEnC.pdf",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Pdf,
//            templateDataC,
//            "EN",
//          )
//        } yield succeed
//      }
//    }
//    "predefinedD" must {
//      "generate word" in {
//        for {
//          (apId, a) <- newReportService
//          x <- a.generateWithTemplateData(
//            Templates.predefinedIdD,
//            apId,
//            "target/reportRuD.docx",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Word,
//            templateDataC,
//            "RU"
//          )
//          y <- a.generateWithTemplateData(
//            Templates.predefinedIdD,
//            apId,
//            "target/reportEnD.docx",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Word,
//            templateDataC,
//            "EN"
//          )
//        } yield succeed
//      }
//      "generate pdf" in {
//        for {
//          (apId, a) <- newReportService
//          x <- a.generateWithTemplateData(
//            Templates.predefinedIdD,
//            apId,
//            "target/reportRuD.pdf",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Pdf,
//            templateDataC,
//            "RU",
//          )
//          y <- a.generateWithTemplateData(
//            Templates.predefinedIdD,
//            apId,
//            "target/reportEnD.pdf",
//            generateUUID,
//            Map.empty,
//            ReportFormatType.Pdf,
//            templateDataC,
//            "EN",
//          )
//        } yield succeed
//      }
//    }
//    "getInfoAll" must {
//      "return all report info" in {
//        for {
//          (apId, a) <- newReportService
//          x <- a.getInfoAll
//        } yield x.size shouldBe Templates.predefined.size
//      }
//    }
//  }
//}