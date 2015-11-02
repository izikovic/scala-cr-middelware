/**
 *
 */
package hr.fer.cr

import hr.fer.model.File

/**
 * @author Zika
 *
 */

object FileRepository {
  private var files: List[File] = List(File(1, "file1", "autor", "desc", 1))

  private def nextId(): Int = if (files.isEmpty) 1 else (files map { _.id }).max + 1

  def add(name: String, author: String, description: String, provider: Int): File = {
    val id = nextId()
    val newFile = File(id, name, author, description, provider)
    files = newFile :: files
    newFile
  }

  def getAll() = files
  
  def getById(id: Int) = files.find(_.id == id)
}