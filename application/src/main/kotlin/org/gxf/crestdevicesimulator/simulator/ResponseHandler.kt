package org.gxf.crestdevicesimulator.simulator

import mu.KotlinLogging
import org.eclipse.californium.core.CoapResponse
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.springframework.stereotype.Component

@Component
class ResponseHandler(private val simulatorProperties: SimulatorProperties, private val pskRepository: PskRepository) {

    private val logger = KotlinLogging.logger {}

    fun handleResponse(response: CoapResponse) {
        val body = String(response.payload)

        if (body.contains("(?s)(?<=PSK:).{16}SET".toRegex())) {
            val setCommand = body.split("(?s)(?<=PSK:).{16}".toRegex()).first()
            handlePskChange(setCommand)
        }
    }

    private fun handlePskChange(setCommand: String) {
        val keyInCommand = "(?s)(?<=PSK:).{16}".toRegex().findAll(setCommand).first().value

        val current = pskRepository.findById(simulatorProperties.pskIdentity)
        if (current.isEmpty) {
            logger.error { "No psk for identity: ${simulatorProperties.pskIdentity}" }
        }

        logger.info { "Setting psk $keyInCommand for ${simulatorProperties.pskIdentity}" }

        pskRepository.save(current.get().apply { key = keyInCommand })
    }
}
