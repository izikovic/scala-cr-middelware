package hr.fer.sp

import akka.actor.{ ActorSystem, Props }
import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory
import scala.util.Try

object SPBoot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "demo-service")

  val config = ConfigFactory.load()

  lazy val host = Try(config.getString("service.host")).getOrElse("localhost")
  lazy val servicePort = 8080

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = host, port = servicePort)
}