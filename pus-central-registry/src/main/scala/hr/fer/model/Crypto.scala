package hr.fer.model

import java.security.PublicKey
import hr.fer.crypto.crypto._
import java.security.PrivateKey

object Crypto {
  def signCertificate(publicKey: Array[Byte], privateKey: PrivateKey): Array[Byte] = {
    val signature = rsa.sign(privateKey, publicKey)

    io.withDataOutputStream("tempCert") { stream =>
      stream.writeInt(publicKey.length)
      stream.write(publicKey)

      stream.writeInt(signature.length)
      stream.write(signature)
    }

    io.readFile("tempCert")
  }
}
