package hr.fer.cr

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import xml.Utility.trim

class MyServiceSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system

  "MyService" should {
    "return something for post" in {
      val request = <provider><name>pr1</name><address><host>localhost</host><port>8080</port></address></provider>
      Post("/providers", request) ~> sealRoute(myRoute) ~> check {
        val res = responseAs[xml.NodeSeq].apply(0)

        trim(res) === trim(<provider>
                       <id>1</id>
                       <name>pr1</name>
                       <address>
                         <host>localhost</host>
                         <port>8080</port>
                       </address>
                     </provider>)
      }
    }
  }
}
