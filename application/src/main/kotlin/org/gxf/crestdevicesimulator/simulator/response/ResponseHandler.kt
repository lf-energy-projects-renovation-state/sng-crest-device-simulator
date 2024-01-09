// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.response

import io.github.oshai.kotlinlogging.KotlinLogging
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

        pskRepository.save(current.get().apply { preSharedKey = newPsk })
        pskStore.key = newPsk
    }
}
