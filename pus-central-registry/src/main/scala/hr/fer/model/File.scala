package hr.fer.model

case class File(id: Int, name: String, author: String, description: String, provider: Int)

object File {
  def fromXml(node: xml.NodeSeq) = {
    val name = (node \\ "name").text
    val author = (node \\ "author").text
    val description = (node \\ "description").text
    val provider = (node \\ "provider").text.toInt
    val id = if ((node \\ "id") != null) (node \\ "id").text.toInt else 0

    File(id, name, author, description, provider)
  }

  def toXml(pr: File): xml.NodeSeq = {
    <file>
      <id>{ pr.id }</id>
      <name>{ pr.name }</name>
      <author>{ pr.author }</author>
      <description>{ pr.description }</description>
      <provider>{ pr.provider }</provider>
    </file>
  }
}