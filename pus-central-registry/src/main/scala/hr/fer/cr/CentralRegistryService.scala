package hr.fer.cr

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import hr.fer.model.Provider
import hr.fer.model.File
import hr.fer.model.Address
import hr.fer.model.Crypto

class MyServiceActor extends Actor with MyService {
  def actorRefFactory = context
  def receive = runRoute(myRoute)
}

trait MyService extends HttpService {

  val myRoute =
    path("providers") {
      get {
        detach() {
          respondWithMediaType(`text/html`) {
            complete {
              <providers>
                { ProviderRepository.getAll() map { Provider.toXml(_) } }
              </providers>
            }
          }
        }
      } ~
        post {
          entity(as[xml.NodeSeq]) { xml =>
            detach() {
              val pr = Provider.fromXml(xml)
              val newPr = ProviderRepository.add(pr.name, pr.address)
              respondWithMediaType(`text/xml`) {
                complete {
                  Provider.toXml(newPr)
                }
              }
            }
          }
        }
    } ~
      path("provider" / IntNumber) { id =>
        get {
          detach() {
            respondWithMediaType(`text/html`) {
              complete {
                Provider.toXml(ProviderRepository.getById(id).getOrElse(Provider(0, "", Address("", 0))))
              }
            }
          }
        }
      } ~
      path("files") {
        get {
          detach() {
            respondWithMediaType(`text/html`) {
              complete {
                <files>
                  { FileRepository.getAll() map { File.toXml(_) } }
                </files>
              }
            }
          }
        } ~
          post {
            entity(as[xml.NodeSeq]) { xml =>
              detach() {
                val filesXml = xml \\ "file"
                val files = (filesXml map { x => File.fromXml(x) }).toList
                files.foreach(pr => FileRepository.add(pr.name, pr.author, pr.description, pr.provider))
                respondWithMediaType(`text/xml`) {
                  complete {
                    <files>{ FileRepository.getAll() map { File.toXml(_) } }</files>
                  }
                }
              }
            }
          }
      } ~
      path("file" / IntNumber) { id =>
        get {
          detach() {
            respondWithMediaType(`text/html`) {
              complete {
                <files>
                  { File.toXml(FileRepository.getById(id).get) }
                </files>
              }
            }
          }
        }
      } ~
      path("getCert") {
        get {
          complete {
            hr.fer.crypto.crypto.io.readFile("CR.pub")
          }
        }
      } ~
      path("signCert") {
        post {
          entity(as[Array[Byte]]) { publicKey =>
            complete {
              Crypto.signCertificate(publicKey, hr.fer.crypto.crypto.io.readPrivateKey("CR.prv"))
            }
          }
        }
      } ~
      path("") {
        get {
          complete {
            <html>
              <body>
                <h1>Ovo je CR</h1>
              </body>
            </html>
          }
        }
      }
}