// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.californium.core.CoapResponse
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.springframework.stereotype.Service

@Service
class FirmwareCommandHandler : CommandHandler {
    private val logger = KotlinLogging.logger {}
    private val numberPartSize = "OTA0000".length

    override fun handleResponse(response: CoapResponse, simulatorState: SimulatorState) {
        val payload = String(response.payload).dropWhile { it == '!' }
        if (!payload.startsWith("OTA")) {
            return
        }

        // If payload contains "OTA", we don't include any other commands
        val otaNumberPart = payload.take(numberPartSize)
        val base85Line = payload.drop(numberPartSize)
        val firmwareDone = payload.endsWith(":DONE")

        logger.debug { "Received OTA line $base85Line" }
        simulatorState.addDownlink(otaNumberPart)
        if (firmwareDone) {
            logger.debug { "Firmware done, resetting FMC" }
            simulatorState.fotaMessageCounter = 0
            simulatorState.addUrc("OTA:SUC")
        } else {
            simulatorState.fotaMessageCounter++
        }
    }
}
