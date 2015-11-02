/**
 *
 */
package hr.fer.cr

import hr.fer.model.Address
import hr.fer.model.Provider

/**
 * @author Zika
 *
 */

object ProviderRepository {
  private var providers: List[Provider] = Nil

  private def nextId(): Int = if (providers.isEmpty) 1 else (providers map { _.id }).max + 1

  def add(name: String, address: Address): Provider = {
    val id = nextId()
    val newProvider = Provider(id, name, address)
    providers = newProvider :: providers
    newProvider
  }

  def getAll() = providers

  def getById(id: Int) = providers.find(_.id == id)
}