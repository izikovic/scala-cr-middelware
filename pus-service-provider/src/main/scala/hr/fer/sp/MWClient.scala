package hr.fer.sp

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.ActorSystem
import hr.fer.sp.SPBoot._
import hr.fer.model.Provider._
import akka.util.Timeout
import akka.pattern.ask
import akka.io.IO
import spray.can.Http
import spray.http._
import spray.routing._
import HttpMethods._
import spray.httpx.unmarshalling._
import hr.fer.model._
import hr.fer.crypto.crypto._
import scala.util.Success
import scala.util.Failure
import spray.client.pipelining._
import scala.concurrent.Await
import scala.concurrent.duration._

object MWClient {

  implicit val timeout: Timeout = Timeout(15.seconds)

  def getFiles(implicit system: ActorSystem) = {
    import system.dispatcher
    val pipeline = sendReceive ~> unmarshal[xml.NodeSeq]
    val response: Future[xml.NodeSeq] = pipeline(Get("http://localhost:" + ProviderInfo.crPort + "/files"))
    response
  }

  def getRemoteFile(name: String, host: String, port: Int)(implicit system: ActorSystem) = {
    import system.dispatcher

    if (verifyCert(host, port)) {
      val pipeline = sendReceive ~> unmarshal[String]
      val getRequest = "http://" + host + ":" + port + "/file/name/" + name
      val response: Future[String] = pipeline(Get(getRequest))
      response
    } else {
      throw new Exception("SP nije verificiran")
    }
  }

  def getLocalFile(name: String) = {
    val source = scala.io.Source.fromFile("""files\""" + name)
    val lines = source.mkString
    println("lines: " + lines)
    source.close()
    lines
  }

  def getProviderAddress(id: Int)(implicit system: ActorSystem) = {
    import system.dispatcher
    val pipeline = sendReceive ~> unmarshal[xml.NodeSeq]
    val response: Future[xml.NodeSeq] = pipeline(Get("http://localhost:" + ProviderInfo.crPort + "/provider/" + id))
    val providerXml = Await.result(response, 5 seconds)
    val providerHost = (providerXml \\ "host").text
    val providerPort = (providerXml \\ "port").text.toInt
    (providerHost, providerPort)
  }

  def getFileById(id: Int)(implicit system: ActorSystem): String = {
    import system.dispatcher
    val pipeline = sendReceive ~> unmarshal[xml.NodeSeq]
    val response: Future[xml.NodeSeq] = pipeline(Get("http://localhost:" + ProviderInfo.crPort + "/file/" + id))
    val fileXml = Await.result(response, 5 seconds)
    val fileName = (fileXml \\ "name").text
    val provider = (fileXml \\ "provider").text.toInt
    if (provider == ProviderInfo.id) {
      getLocalFile(fileName)
    } else {
      println("tu")
      val (host, port) = getProviderAddress(provider)
      println("host: " + host + " port: " + port + " filename: " + fileName)
      Await.result(getRemoteFile(fileName, host, port), 5 seconds)
    }
  }

  def register(name: String, host: String, port: Int)(implicit system: ActorSystem) = {
    import system.dispatcher
    val pipeline = sendReceive ~> unmarshal[xml.NodeSeq]
    val providerReq = Provider.toXml(Provider(0, name, Address(host, port)))
    val response: Future[xml.NodeSeq] = pipeline(Post("http://localhost:" + ProviderInfo.crPort + "/providers", providerReq))
    response
  }

  def updateFiles(implicit system: ActorSystem) = {
    import system.dispatcher
    val pipeline = sendReceive ~> unmarshal[xml.NodeSeq]

    import java.io.{ File => JavaFile }
    val fileNames: List[String] = (new JavaFile("files")).listFiles.toList map { x => x.getName }

    val request: xml.NodeSeq =
      <files>
        { fileNames map { f => File.toXml(File(0, f, "autor1", f, ProviderInfo.id)) } }
      </files>

    val response: Future[xml.NodeSeq] = pipeline(Post("http://localhost:" + ProviderInfo.crPort + "/files", request))
    response
  }

  def getCertificate(implicit system: ActorSystem) = {
    import system.dispatcher
    val pipeline = sendReceive ~> unmarshal[Array[Byte]]
    val response: Future[Array[Byte]] = pipeline(Get("http://localhost:" + ProviderInfo.crPort + "/getCert"))

    response onComplete {
      case Success(crCert) => hr.fer.crypto.crypto.io.writeFile("CRcert", crCert)
      case Failure(f) => throw new Exception("greska u dohvatu certifikata: " + f)
    }
    //val crCert = Await.result(response, 10 seconds)    
  }

  def signCertificate(implicit system: ActorSystem) = {
    import system.dispatcher
    val pipeline = sendReceive ~> unmarshal[Array[Byte]]

    val publicKey = hr.fer.crypto.crypto.io.readFile(ProviderInfo.publicKey)

    val response: Future[Array[Byte]] = pipeline(Post("http://localhost:" + ProviderInfo.crPort + "/signCert", publicKey))

    response onComplete {
      case Success(signedCert) => hr.fer.crypto.crypto.io.writeFile(ProviderInfo.signedCert, signedCert)
      case Failure(f) => throw new Exception("greska u dohvatu certifikata: " + f)
    }

    //val signedCert = Await.result(response, 10 seconds)    
  }

  def getSignedCert = {
    io.readFile(ProviderInfo.signedCert)
  }

  def verifyCert(host: String, port: Int)(implicit system: ActorSystem): Boolean = {
    import system.dispatcher
    val pipeline = sendReceive ~> unmarshal[Array[Byte]]

    val signedCert = hr.fer.crypto.crypto.io.readFile(ProviderInfo.signedCert)

    val response: Future[Array[Byte]] = pipeline(Post("http://" + host + ":" + port + "/verify", signedCert))
    val receivedCert = Await.result(response, 30 seconds)
    writeReceivedCert(receivedCert)
    ver()
  }

  def writeReceivedCert(cert: Array[Byte]): Unit = {
    io.writeFile("receivedCert", cert)
  }

  def ver(): Boolean = {
    val publicKeyCA = io.readPublicKey("CRcert")
    val (signature, publicKeySP2) = io.readSignatureFromCert("receivedCert")
    val ok = rsa.verify(publicKeyCA, signature, publicKeySP2)
    println("verificiran=" + ok)
    ok
  }
}