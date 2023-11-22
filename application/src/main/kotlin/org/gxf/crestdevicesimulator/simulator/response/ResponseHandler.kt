package org.gxf.crestdevicesimulator.simulator.response

import mu.KotlinLogging
import org.eclipse.californium.core.CoapResponse
import org.gxf.crestdevicesimulator.configuration.AdvancedSingleIdentityPskStore
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.springframework.stereotype.Component

@Component
class ResponseHandler(private val simulatorProperties: SimulatorProperties,
                      private val pskRepository: PskRepository,
                      private val pskKeyExtractor: PskKeyExtractor,
                      private val pskStore: AdvancedSingleIdentityPskStore) {

    private val logger = KotlinLogging.logger {}

    fun handleResponse(response: CoapResponse) {
        val body = String(response.payload)

        if (pskKeyExtractor.hasPskCommand(body)) {
            val newPsk = pskKeyExtractor.extractKeyFromCommand(body)
            handlePskChange(newPsk)
        }
    }

    private fun handlePskChange(newPsk: String) {
        val current = pskRepository.findById(simulatorProperties.pskIdentity)
        if (current.isEmpty) {
            logger.error { "No psk for identity: ${simulatorProperties.pskIdentity}" }
        }

        logger.info { "Setting psk $newPsk for ${simulatorProperties.pskIdentity}" }

        pskRepository.save(current.get().apply { key = newPsk })
        pskStore.key = newPsk
    }
}
