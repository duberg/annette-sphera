package annette.app

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import annette.app.exchange._
import annette.app.exchange.actor.ExchangeServiceActor
import annette.app.exchange.client.FixerIoClient
import annette.app.http.routes.ExchangeRoutes
import com.typesafe.config.{ Config, ConfigFactory }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor }
import scala.concurrent.duration._
import annette.app.exchange.Exchange._

class ExchangeRoutesSpec extends WordSpec
  with ExchangeRoutes
  with Matchers
  with ScalaFutures
  with ScalatestRouteTest {

  implicit val t: FiniteDuration = 4.minute
  // implicit val c: ExecutionContext = system.dispatcher

  val config: Config = ConfigFactory.load()
  val client = FixerIoClient(config.getConfig("annette.exchange.client.fixerio"))(system, materializer, system.dispatcher)
  val exchangeServiceActor = system.actorOf(ExchangeServiceActor.props(client)(executor, 4.minutes))
  val exchangeService = new ExchangeService(exchangeServiceActor)(executor, 4.minutes)
  val routes = exchangeRoutes

  "ExchangeRoutesSpec" should {
    "get rates" in {

      implicit val timeout = RouteTestTimeout(1.minute)

      Get("/exchange") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val response = entityAs[Rates]
        response.base shouldBe "EUR"
        response.rates should not be empty
      }
    }

    "return zero values for unknown currencies" in {
      Post("/exchange", Convert(List(ConvertEntry("USB", "RUB", 100)))) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)

        val response = entityAs[ConvertRes]

        response.errorCode shouldBe 0
        response.data.head.valueTo shouldBe 0
      }
    }

    "return equal values for same currency" in {
      Post("/exchange", Convert(List(ConvertEntry("EUR", "EUR", 100)))) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)

        val response = entityAs[ConvertRes]

        response.errorCode shouldBe 0
        response.data.head.valueTo shouldBe 100
      }
    }

    "convert 100 USD to EUR" in {
      Post("/exchange", Convert(List(ConvertEntry("USD", "EUR", 100)))) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)

        val response = entityAs[ConvertRes]

        info(s"100 USD = ${response.data.head.valueTo} EUR")

        response.errorCode shouldBe 0
      }
    }

  }

}