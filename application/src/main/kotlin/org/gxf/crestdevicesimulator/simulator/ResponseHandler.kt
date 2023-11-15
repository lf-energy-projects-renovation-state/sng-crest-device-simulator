package org.gxf.crestdevicesimulator.simulator

import mu.KotlinLogging
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.springframework.stereotype.Component

@Component
class ResponseHandler(private val simulatorProperties: SimulatorProperties, private val pskRepository: PskRepository) {

    private val logger = KotlinLogging.logger {}

    fun handleResponse(response: String) {
        if (response.contains("(?s)(?<=PSK:).{16}SET".toRegex())) {
            val setCommand = response.split("(?s)(?<=PSK:).{16}".toRegex(), limit = 1).first()
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
