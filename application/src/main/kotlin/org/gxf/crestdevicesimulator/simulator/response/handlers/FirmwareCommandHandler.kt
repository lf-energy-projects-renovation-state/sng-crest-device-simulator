// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.californium.core.CoapResponse
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.springframework.stereotype.Service

@Service
class FirmwareCommandHandler : CommandHandler {
    private val logger = KotlinLogging.logger {}

    override fun handleResponse(response: CoapResponse, simulatorProperties: SimulatorProperties): String {
        val payload = String(response.payload)
        val otaLine = payload.split(";", "!").firstOrNull { it.startsWith("OTA") }?.split(":")?.lastOrNull()
        val firmwareDone = payload.endsWith(":DONE")

        if (otaLine != null) {
            logger.debug { "Received OTA line $otaLine" }
            if (firmwareDone) {
                logger.debug { "Firmware done, resetting FMC" }
                simulatorProperties.fotaMessageCounter = 0
            } else {
                simulatorProperties.fotaMessageCounter++
            }
        }

        return if (firmwareDone) "OTA:SUC" else ""
    }
}
