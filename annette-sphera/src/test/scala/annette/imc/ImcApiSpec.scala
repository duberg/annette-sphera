package annette.imc

import java.util.UUID

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.BasicDirectives.provide
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Source
import akka.util.ByteString
import annette.core.CoreModule
import annette.core.security.authentication.Session
import annette.core.inject.AkkaModule
import annette.core.test.InMemoryCleanup
import annette.imc.http.ImcApi
import annette.imc.model.ApFile.FileType
import annette.imc.model._
import annette.imc.user.model.UserRoled
import annette.imc.utils.Implicits._
import com.google.inject.Guice
import com.typesafe.config.ConfigFactory
import io.circe.parser._
import org.scalatest.{Matchers, WordSpec}
import com.google.inject.Guice
import com.typesafe.config.{Config, ConfigException}
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

class ImcApiSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with InMemoryCleanup {
  import io.circe.generic.auto._
  import io.circe.syntax._

  def getUserRoled(ec: ExecutionContext)(userId: UUID) = {
    Future(Some(
      UserRoled(
        UUID.fromString("14c0609c-cb01-482c-b39b-5bc2d8ff86fa"),
        "Иванов", "Иван", "Иванович", None, true, true, true, true, true)))
  }

  val ctx = ImcContext(system, ConfigFactory.load(), System.getProperty("java.io.tmpdir"), getUserRoled(system.dispatcher))

  val sd: Session = Session(UUID.randomUUID(), UUID.randomUUID(), "IBM", "imc", "Ru")

  val authent: Directive1[Session] = provide(sd)

  val injector = Guice.createInjector(new AkkaModule())

  val coreModule = injector.instance[CoreModule]

  val api = new ImcApi(coreModule, ctx, authent)

  "The api" should {
    var apId: String = ""
    "return a greeting for GET requests to the root path" in {

      Get("/imc/api/ap/new1") ~> api.routes ~> check {
        apId = responseAs[String].replaceAll("\"", "")
        response.status.toString() shouldBe "200 OK"
      }

      Get("/imc/api/ap/all") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[List[ApSimple]]).map(_.size) shouldBe Right(1)
      }

      Get("/imc/api/ap/get/update/" + apId) ~> api.routes ~> check {
        //        val n = ApString("про", "pro")
        //        val f = Financing(Financing.LOANS)
        //        val p = ApString("Иванов", "Ivanov")
        //
        //        val o = Set(OperationType(OperationType.CLINIC), OperationType(OperationType.CARDIO), OperationType(OperationType.SCIENCE,
        //          Some(ApString("ла", "lo"))))
        //        val u = UpdateAp(id=UUID.fromString(apId),
        //          entityName = Some(n),
        //          personName = Some(p),
        //          financing = Some(f),
        //          operationTypes = Some(o)).asJson
        //        println(u)
        parse(responseAs[String]).flatMap(_.as[UpdateAp].map(_.id.toString)) shouldBe Right(apId)
      }

      val updateString =
        s"""
          |
          |{
          |  "id" : "$apId",
          |  "entityName" : {
          |    "ru" : "про",
          |    "en" : "pro"
          |  },
          |  "personName" : {
          |    "ru" : "Иванов",
          |    "en" : "Ivanov"
          |  },
          |  "personPosition" : null,
          |  "personEmail" : null,
          |  "personTel" : null,
          |  "country" : null,
          |  "operationTypes" : null,
          |  "financing" : [{
          |    "nameMessage" : "imc.ap.financing.loans"
          |  }],
          |  "purpose" : "med"
          |}
        """.stripMargin

      val updateString1 =
        s"""
           |
           |{
           |  "id" : "${UUID.randomUUID()}",
           |  "entityName" : {
           |    "ru" : "про",
           |    "en" : "pro"
           |  },
           |  "personName" : {
           |    "ru" : "Иванов",
           |    "en" : "Ivanov"
           |  },
           |  "personPosition" : null,
           |  "personEmail" : null,
           |  "personTel" : null,
           |  "country" : null,
           |  "operationTypes" : null,
           |  "financing" : [{
           |    "nameMessage" : "imc.ap.financing.loans"
           |  }]
           |}
        """.stripMargin

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/imc/api/ap/update",
        entity = HttpEntity(MediaTypes.`application/json`, updateString))

      postRequest ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      val postRequest1 = HttpRequest(
        HttpMethods.POST,
        uri = "/imc/api/ap/update",
        entity = HttpEntity(MediaTypes.`application/json`, updateString1))

      postRequest1 ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "ApNotExists"
      }

      Get("/imc/api/ap/get/update/" + apId) ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[UpdateAp].map(_.entityName.map(_.en))).toOption.flatten shouldBe Some("pro")
        parse(responseAs[String]).flatMap(_.as[UpdateAp].map(_.purpose)).toOption.flatten shouldBe Some("med")

      }

      val expertId = UUID.randomUUID()

      Get(s"/imc/api/expert/add/$apId/$expertId") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }
      Get(s"/imc/api/expert/add/$apId/$expertId") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }
      Get(s"/imc/api/expert/add/$apId/${UUID.randomUUID()}") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/expert/all/$apId") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[List[String]].map(_.size)) shouldBe Right(2)
      }
      Get(s"/imc/api/expert/remove/$apId/${UUID.randomUUID()}") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "NotFound"
      }
      Get(s"/imc/api/expert/remove/$apId/$expertId") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }
      Get(s"/imc/api/expert/all/$apId") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[List[String]].map(_.size)) shouldBe Right(1)
      }
      //    }

      val multipartForm1 =
        Multipart.FormData(
          Multipart.FormData.BodyPart(
            "file",
            HttpEntity(ContentTypes.`application/octet-stream`, 9, Source.single(ByteString("blah-blah"))),
            Map("filename" -> "blah.txt")))

      val multipartForm2 =
        Multipart.FormData(
          Multipart.FormData.BodyPart(
            "file",
            HttpEntity(ContentTypes.`application/octet-stream`, 14, Source.single(ByteString("blah-blah-blah"))),
            Map("filename" -> "blah-blah.txt")))

      //    "be able to upload file" in {
      //      val path: Path = Paths.get("/tmp/test.txt")
      //      val formData = Multipart.FormData.fromPath("file", ContentTypes.`application/octet-stream`, path, 100000)
      Post(s"/imc/api/file/upload/$apId", multipartForm1) ~> api.routes ~> check {
        status shouldBe StatusCodes.OK
        //        println(parse(responseAs[String]).flatMap(_.as[ApFile]))
        //        responseAs[String] contains "File successfully uploaded"
      }

      Post(s"/imc/api/file/upload/$apId", multipartForm2) ~> api.routes ~> check {
        status shouldBe StatusCodes.OK
        //        responseAs[String] contains "File successfully uploaded" shouldBe true
      }

      var (file1, file2) = ("", "")

      Get(s"/imc/api/ap/get/files/$apId") ~> api.routes ~> check {
        //        status shouldBe StatusCodes.OK
        //        println(parse(responseAs[String]).flatMap(_.as[List[(String, String)]]))
        val files = parse(responseAs[String]).flatMap(_.as[List[ApFile]])

        file1 = files match {
          case Right(x) => x.head.id.toString
          case Left(_) => ""
        }

        file2 = files match {
          case Right(x) => x.last.id.toString
          case Left(_) => ""
        }

        parse(responseAs[String]).flatMap(_.as[List[ApFile]]).map(_.size) shouldBe Right(2)

      }

      Get(s"/imc/api/file/get/$apId/$file1") ~> api.routes ~> check {

        parse(responseAs[String]).flatMap(_.as[ApFile]).map(_.name) shouldBe Right("blah.txt")
      }

      Get(s"/imc/api/file/get/$apId/${UUID.randomUUID()}") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "FileNotExists"
      }

      val fileUpdate = ApFile(UUID.fromString(file1), "blah.txt", "Ru", FileType(FileType.ChartFile))

      val postRequest2 = HttpRequest(
        HttpMethods.POST,
        uri = s"/imc/api/file/update/$apId",
        entity = HttpEntity(MediaTypes.`application/json`, fileUpdate.asJson.toString()))

      postRequest2 ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/file/get/$apId/$file1") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[ApFile]).map(_.fileType) shouldBe Right(FileType(FileType.ChartFile))
      }

      Get(s"/imc/api/file/remove/$apId/$file1") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      //      Get(s"/imc/api/file/remove/${UUID.randomUUID()}/$file2") ~> api.routes ~> check {
      //        responseAs[String].replaceAll("\"", "") shouldBe "ApNotExists"
      //      }

      Get(s"/imc/api/ap/get/files/$apId") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[List[ApFile]]).map(_.size) shouldBe Right(1)
      }

      val criterionUpdate1 = UpdateCriterion(1, Some("Ля-ля-ля"))
      val criterionUpdate2 = UpdateCriterion(1, None, Some("Blah-blah-blah"))

      val postRequest3 = HttpRequest(
        HttpMethods.POST,
        uri = s"/imc/api/criterion/update/$apId",
        entity = HttpEntity(MediaTypes.`application/json`, criterionUpdate1.asJson.toString()))

      postRequest3 ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      val postRequest4 = HttpRequest(
        HttpMethods.POST,
        uri = s"/imc/api/criterion/update/$apId",
        entity = HttpEntity(MediaTypes.`application/json`, criterionUpdate2.asJson.toString()))

      postRequest4 ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/criterion/get/$apId/1") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[Criterion]).toOption.map(_.description.ru) shouldBe Some("Ля-ля-ля")
        parse(responseAs[String]).flatMap(_.as[Criterion]).toOption.map(_.description.en) shouldBe Some("Blah-blah-blah")
      }

      Get(s"/imc/api/criterion/get/$apId/10") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "CriterionNotExists"
      }

      Get(s"/imc/api/criterion/getList/$apId") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[List[Criterion]]).map(_.size) shouldBe Right(1)

      }

      Get(s"/imc/api/criterion/addFile/$apId/1/$file2") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/criterion/addFile/$apId/1/$file1") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "FileNotExists"
      }

      Get(s"/imc/api/criterion/addFile/$apId/1/$file2") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/criterion/addFile/$apId/2/$file2") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "CriterionNotExists"
      }

      Get(s"/imc/api/criterion/get/files/$apId/1") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[List[ApFile]]).map(_.size) shouldBe Right(1)
      }

      Get(s"/imc/api/criterion/get/files/$apId/2") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "CriterionNotExists"
      }

      Get(s"/imc/api/criterion/removeFile/$apId/2/$file2") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "CriterionNotExists"
      }

      Get(s"/imc/api/criterion/removeFile/$apId/1/$file2") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/criterion/get/files/$apId/1") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[List[ApFile]]).map(_.size) shouldBe Right(0)
      }

      //      Get(s"/imc/api/criterion/clean/$apId/1") ~> api.routes ~> check {
      //        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      //      }
      //
      //      Get(s"/imc/api/criterion/clean/$apId/1") ~> api.routes ~> check {
      //        responseAs[String].replaceAll("\"", "") shouldBe "CriterionNotExists"
      //      }

      //      Get(s"/imc/api/criterion/getList/$apId") ~> api.routes ~> check {
      //        parse(responseAs[String]).flatMap(_.as[List[Criterion]]).map(_.size) shouldBe Right(0)
      //      }
      postRequest4 ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/criterion/getList/$apId") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[List[Criterion]]).map(_.size) shouldBe Right(1)
      }

      Get(s"/imc/api/criterion/finish/$apId/1") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/criterion/finish/$apId/2") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "CriterionNotExists"
      }

      Get(s"/imc/api/criterion/get/$apId/1") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[Criterion]).toOption.map(_.isFinished) shouldBe Some(true)

      }

      Get(s"/imc/api/criterion/finish/$apId/1") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/criterion/get/$apId/1") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[Criterion]).toOption.map(_.isFinished) shouldBe Some(false)

      }

      Get(s"/imc/api/criterion/finish/$apId/1") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/expert/add/$apId/$expertId") ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }
      //     MUST RE-WRITE
      //      val updateBulletin = UpdateBulletin(expertId, criterions = Some(Map(1 -> Vote("bla", true, "la"))))
      //      val postRequest5 = HttpRequest(
      //        HttpMethods.POST,
      //        uri = s"/imc/api/bulletin/update/$apId/$expertId",
      //        entity = HttpEntity(MediaTypes.`application/json`, updateBulletin.asJson.toString()))
      //
      //      postRequest5 ~> api.routes ~> check {
      //        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      //      }
      //
      //      Get(s"/imc/api/bulletin/vote/$apId/$expertId") ~> api.routes ~> check {
      //        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      //      }

      Get(s"/imc/api/status/get/$apId") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[ApStatus]).map(_.nameMessage) shouldBe Right(ApStatus.FILLING)
      }

      //      Get(s"/imc/api/status/ready/$apId") ~> api.routes ~> check {
      //        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      //      }

      //      Get(s"/imc/api/status/get/$apId") ~> api.routes ~> check {
      //        parse(responseAs[String]).flatMap(_.as[ApStatus]).map(_.nameMessage) shouldBe Right(ApStatus.FILLED)
      //      }

      val json =
        """
          |{ "comment" : "It's all bad"}
        """.stripMargin

      //      val postRequest6 = HttpRequest(
      //        HttpMethods.POST,
      //        uri = s"/imc/api/status/notReady/$apId",
      //        entity = HttpEntity(MediaTypes.`application/json`, json))
      //
      //      postRequest6 ~> api.routes ~> check {
      //        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      //      }

      //      Get(s"/imc/api/status/get/$apId") ~> api.routes ~> check {
      //        parse(responseAs[String]).flatMap(_.as[ApStatus]).map(_.nameMessage) shouldBe Right(ApStatus.FILLING)
      //        parse(responseAs[String]).flatMap(_.as[ApStatus]).toOption.flatMap(_.comment) shouldBe Some("It's all bad")
      //      }

      //      Get(s"/imc/api/status/toExpertise/$apId") ~> api.routes ~> check {
      //        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      //      }
      //
      //      Get(s"/imc/api/status/get/$apId") ~> api.routes ~> check {
      //        parse(responseAs[String]).flatMap(_.as[ApStatus]).map(_.nameMessage) shouldBe Right(ApStatus.ONEXPERTISE)
      //      }

      val result = ApResult(ApString("ляляля", "blah")).asJson.toString()

      val postRequest7 = HttpRequest(
        HttpMethods.POST,
        uri = s"/imc/api/status/accomplished/$apId",
        entity = HttpEntity(MediaTypes.`application/json`, result))

      postRequest7 ~> api.routes ~> check {
        responseAs[String].replaceAll("\"", "") shouldBe "Done"
      }

      Get(s"/imc/api/status/get/$apId") ~> api.routes ~> check {
        parse(responseAs[String]).flatMap(_.as[ApStatus]).map(_.nameMessage) shouldBe Right(ApStatus.ACCOMPLISHED)
        parse(responseAs[String]).flatMap(_.as[ApStatus]).toOption.flatMap(_.result).map(_.decision.en) shouldBe Some("blah")
      }

      Get(s"/imc/api/file/download/$apId/$file1") ~> api.routes ~> check {
        response.status.toString() shouldBe "200 OK"
      }

      Get(s"/imc/api/file/download/$apId/${UUID.randomUUID()}") ~> api.routes ~> check {
        response.status.toString() shouldBe "200 OK"
      }
      //      val userId = UUID.fromString("535c5063-71af-4182-94be-8161df2d27d0")
      //      Get(s"/imc-admin/update/535c5063-71af-4182-94be-8161df2d27d0") ~> api.routes ~> check {
      //        response.status.toString() shouldBe "200 OK"
      //      }

    }
  }
}
