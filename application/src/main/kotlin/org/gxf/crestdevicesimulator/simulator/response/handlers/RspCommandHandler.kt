// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.springframework.stereotype.Service

/**
 * Command Handler for CMD:RSP downlink
 *
 * Disables sleep mode of the telecom modem for 1 hour for Remote Sim Provisioning (RSP) (GSM connection enabled + data
 * connection disabled)
 * - On success: "RSP:OK" URC will be returned in the next message sent
 * - On failure: "RSP:DLER" URC will be returned in the next message sent
 */
@Service
class RspCommandHandler : CommandHandler {
    private val logger = KotlinLogging.logger {}

    override fun canHandleCommand(command: String) = command == CMD_RSP

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        if (!canHandleCommand(command)) {
            logger.warn { "RSP command handler can not handle command: $command" }
            return
        }
        try {
            handleRspCommand(command, simulatorState)
        } catch (ex: Exception) {
            handleFailure(command, simulatorState)
        }
    }

    private fun handleRspCommand(command: String, simulatorState: SimulatorState) {
        logger.info { "Handling RSP command: $command" }
        simulatorState.addUrc(URC_SUCCESS)
        simulatorState.addDownlink(CMD_RSP)
    }

    private fun handleFailure(command: String, simulatorState: SimulatorState) {
        logger.warn { "Handling failure for RSP command: $command" }
        simulatorState.addUrc(URC_ERROR)
        simulatorState.addDownlink(CMD_RSP)
    }

    companion object {
        private const val CMD_RSP = "CMD:RSP"
        private const val URC_SUCCESS = "RSP:OK"
        private const val URC_ERROR = "RSP:DLER"
    }
}
