package annette.core.akkaext.http

import akka.http.scaladsl.model.StatusCodes

class PaginationDirectivesWithDefaultsAndAlwaysFallback extends PaginationSpec {

  override def testConfigSource =
    """akka.http.extensions.pagination.defaults.enabled = true
      | akka.http.extensions.pagination.defaults.always-fallback = true
      | akka.http.extensions.pagination.defaults.offset = 0
      | akka.http.extensions.pagination.defaults.limit = 50
    """.stripMargin

  "Pagination with defaults and fallback" should "have a page with defaults if none was requested" in {

    Get("/filter-test") ~> paginationRoute ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual PageRequest(0, 50, Map.empty).toString
    }

    Get("/filter-test") ~> paginationOrDefaultsRoute ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual PageRequest(0, 50, Map.empty).toString
    }
  }

  it should "have a page with defaults if one of the parameters is set" in {
    Get("/filter-test?offset=1") ~> paginationRoute ~> check {
      responseAs[String] shouldEqual PageRequest(1, 50, Map.empty).toString
    }

    Get("/filter-test?limit=100") ~> paginationRoute ~> check {
      responseAs[String] shouldEqual PageRequest(0, 100, Map.empty).toString
    }

    Get("/filter-test?offset=1") ~> paginationOrDefaultsRoute ~> check {
      responseAs[String] shouldEqual PageRequest(1, 50, Map.empty).toString
    }

    Get("/filter-test?limit=100") ~> paginationOrDefaultsRoute ~> check {
      responseAs[String] shouldEqual PageRequest(0, 100, Map.empty).toString
    }
  }

  it should "return the page object that was requested" in {
    Get("/filter-test?offset=1&limit=10") ~> paginationRoute ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual PageRequest(1, 10, Map.empty).toString
    }

    Get("/filter-test?offset=1&limit=10") ~> paginationOrDefaultsRoute ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual PageRequest(1, 10, Map.empty).toString
    }
  }

  it should "return the page object with sorting that was requested" in {
    Get("/filter-test?offset=1&limit=10&sort=name,asc;age,desc") ~> paginationRoute ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual PageRequest(1, 10, Map("name" -> Order.Asc, "age" -> Order.Desc)).toString
    }

    Get("/filter-test?offset=1&limit=10&sort=name,asc;age,desc") ~> paginationOrDefaultsRoute ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual PageRequest(1, 10, Map("name" -> Order.Asc, "age" -> Order.Desc)).toString
    }
  }
}