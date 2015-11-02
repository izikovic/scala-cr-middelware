package hr.fer.sp

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import scala.concurrent.Await
import scala.concurrent.duration._
import hr.fer.model.File

class MyServiceActor extends Actor with MyService {
  def actorRefFactory = context
  def receive = runRoute(myRoute)

  private implicit def executionContext = actorRefFactory.dispatcher
  private implicit val system = SPBoot.system
  val res = Await.result(MWClient.register("name2", SPBoot.host, SPBoot.servicePort), 5 seconds)
  ProviderInfo.id = (res \ "id").text.toInt
  MWClient.signCertificate
  MWClient.getCertificate
  MWClient.updateFiles
  "registriran; id = " + ProviderInfo.id
}

trait MyService extends HttpService {

  private implicit def executionContext = actorRefFactory.dispatcher
  private implicit val system = SPBoot.system

  val myRoute =
    path("files") {
      get {
        detach() {
          complete {
            val filesXml = Await.result(MWClient.getFiles, 5 seconds)

            //filesXml

            val files = filesXml \\ "file"

            <html>
              <body>
                {
                  files map { fh =>
                    val f = File.fromXml(fh)
                    <div>
                      <p> { f.name } </p>
                      <p> { f.provider } </p>
                      <a href={ "file/id/" + f.id }>link</a>
                    </div>
                  }
                }
              </body>
            </html>

          }
        }
      }
    } ~
      path("file" / "id" / IntNumber) { id =>
        get {
          detach() {
            complete {
              MWClient.getFileById(id)
            }
          }
        }
      } ~
      path("file" / "name" / RestPath) { name =>
        get {
          detach() {
            complete {
              println(name.toString)
              MWClient.getLocalFile(name.toString)
            }
          }
        }
      } ~
      path("reg") {
        get {
          complete {
            ProviderInfo.id match {
              case 0 => {
                println(ProviderInfo.id)
                val res = Await.result(MWClient.register("name2", SPBoot.host, SPBoot.servicePort), 5 seconds)
                ProviderInfo.id = (res \ "id").text.toInt
                MWClient.signCertificate
                MWClient.getCertificate
                "registriran; id = " + ProviderInfo.id
              }
              case x => {
                println(ProviderInfo.id)
                "vec je registriran; id = " + x
              }
            }
          }
        }
      } ~
      path("upd") {
        get {
          complete {
            MWClient.updateFiles
          }
        }
      } ~
      path("sign") {
        get {
          complete {
            MWClient.signCertificate
            "OK"
          }
        }
      } ~
      path("verify") {
        post {
          entity(as[Array[Byte]]) { cert =>
            complete {
              MWClient.writeReceivedCert(cert)
              println("res: " + MWClient.ver)
              MWClient.getSignedCert
            }
          }
        }
      } ~
      path("test") {
        get {
          complete {
            println("pocelo")
            MWClient.verifyCert("localhost", 8081)
            "bla"
          }
        }
      } ~
      path("") {
        get {
          complete {
            <html>
              <body>
                <h1>Ovo je SP</h1>
              </body>
            </html>
          }
        }
      }
}