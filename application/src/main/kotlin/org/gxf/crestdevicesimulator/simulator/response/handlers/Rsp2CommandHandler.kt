// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.springframework.stereotype.Service

/**
 * Command Handler for CMD:RSP2 downlink Disables sleep mode of the telecom modem for 1 hour for Remote Sim Provisioning
 * (RSP) (GSM connection enabled + data connection enabled) On success: "CMD:RSP2" URC will be returned in the next
 * message sent On failure: "CMD:DLER" URC will be returned in the next message sent
 */
@Service
class Rsp2CommandHandler : CommandHandler {
    private val logger = KotlinLogging.logger {}

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        if (canHandleCommand(command)) {
            try {
                handleRsp2Command(command, simulatorState)
            } catch (ex: Exception) {
                handleFailure(command, simulatorState)
            }
        }
    }

    private fun canHandleCommand(command: String) = command == CMD_RSP2

    private fun handleRsp2Command(command: String, simulatorState: SimulatorState) {
        logger.info { "Handling RSP2 command: $command" }
        simulatorState.addUrc(URC_SUCCESS)
        simulatorState.addDownlink(CMD_RSP2)
    }

    private fun handleFailure(command: String, simulatorState: SimulatorState) {
        logger.warn { "Handling failure for RSP2 command: $command" }
        simulatorState.addUrc(URC_ERROR)
        simulatorState.addDownlink(CMD_RSP2)
    }

    companion object {
        private const val CMD_RSP2 = "CMD:RSP2"
        private const val URC_SUCCESS = "RSP2:OK"
        private const val URC_ERROR = "RSP2:DLER"
    }
}
