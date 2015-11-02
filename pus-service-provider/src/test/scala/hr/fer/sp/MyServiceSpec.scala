package hr.fer.sp

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

//class MyServiceSpec extends Specification with Specs2RouteTest with MyService {
//  def actorRefFactory = system
//
//  "MyService" should {
//
//    "return GET 5 for order/5" in {
//      Get("/order/5") ~> myRoute ~> check {
//        responseAs[String] must contain("GET 5")
//      }
//    }
//
//    "leave GET requests to other paths unhandled" in {
//      Get("/kermit") ~> myRoute ~> check {
//        handled must beFalse
//      }
//    }
//
//    "return PUT 5 for order/5 put" in {
//      Put("/order/5") ~> sealRoute(myRoute) ~> check {
//        responseAs[String] === "PUT 5"
//      }
//    }
//  }
//}
