// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.springframework.stereotype.Service

@Service
class FirmwareCommandHandler : CommandHandler {
    private val logger = KotlinLogging.logger {}

    private val numberPartSize = "OTA0000".length

    override fun canHandleCommand(command: String) = command.startsWith("OTA")

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        require(canHandleCommand(command)) { "Firmware command handler can not handle command: $command" }

        // If payload contains "OTA", we don't include any other commands
        val otaNumberPart = command.take(numberPartSize)
        val base85Line = command.drop(numberPartSize)
        val firmwareDone = command.endsWith(":DONE")

        logger.debug { "Received OTA line $base85Line" }
        simulatorState.addDownlink(otaNumberPart)
        if (firmwareDone) {
            logger.debug { "Firmware done, resetting FMC and sending OTA:SUC URC" }
            simulatorState.fotaMessageCounter = 0
            simulatorState.addUrc("OTA:SUC")
        } else {
            simulatorState.fotaMessageCounter++
        }
    }
}
