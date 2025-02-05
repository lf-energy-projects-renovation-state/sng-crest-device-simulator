// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskHashException
import org.gxf.crestdevicesimulator.simulator.response.psk.PskService
import org.springframework.stereotype.Service

/**
 * Command Handler for PSK downlink Sets a temporary pre shared key (PSK) On success: "PSK:TMP" URC will be returned in
 * the next message sent On failure: "PSK:DLER" or "PSK:HSER" URC will be returned in the next message sent
 */
@Service
class PskCommandHandler(private val pskService: PskService) : CommandHandler {
    private val logger = KotlinLogging.logger {}
    private val commandRegex = "^(?!.*:SET)PSK:(?<key>[a-zA-Z0-9]{16}):(?<hash>[a-zA-Z0-9]{64})".toRegex()

    override fun canHandleCommand(command: String) = commandRegex.matches(command)

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        require(canHandleCommand(command)) { "PSK command handler can not handle command: $command" }

        try {
            handlePskCommand(command, simulatorState)
        } catch (ex: Exception) {
            handleFailure(command, simulatorState, ex)
        }
    }

    private fun handlePskCommand(command: String, simulatorState: SimulatorState) {
        logger.info { "Handling PSK command: $command" }
        pskService.preparePendingKey(command)
        simulatorState.addUrc(URC_PSK_SUCCESS)
        simulatorState.addDownlink(DOWNLINK_PSK)
    }

    private fun handleFailure(command: String, simulatorState: SimulatorState, ex: Exception) {
        logger.error(ex) { "Handling failure for PSK command: $command" }
        when (ex) {
            is InvalidPskHashException -> simulatorState.addUrc(URC_PSK_HASH_ERROR)
            else -> simulatorState.addUrc(URC_PSK_DOWNLINK_ERROR)
        }
        simulatorState.addDownlink(DOWNLINK_PSK)

        pskService.setPendingKeyAsInvalid()
    }

    companion object {
        private const val URC_PSK_SUCCESS = "PSK:TMP"
        private const val URC_PSK_HASH_ERROR = "PSK:HSER"
        private const val URC_PSK_DOWNLINK_ERROR = "PSK:DLER"
        private const val DOWNLINK_PSK = "PSK:################"
    }
}
