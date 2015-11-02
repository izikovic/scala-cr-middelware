package hr.fer.model

case class Address(host: String, port: Int)

case class Provider(id: Int, name: String, address: Address)

object Provider {
  def fromXml(node: xml.NodeSeq) = {
    val name = (node \\ "name").text
    val host = (node \\ "address" \ "host").text
    val port = (node \\ "address" \ "port").text.toInt

    Provider(0, name, Address(host, port))
  }

  def toXml(pr: Provider): xml.NodeSeq = {
    <provider>
      <id>{ pr.id }</id>
      <name>{ pr.name }</name>
      <address>
        <host>{ pr.address.host }</host>
        <port>{ pr.address.port }</port>
      </address>
    </provider>
  }
}