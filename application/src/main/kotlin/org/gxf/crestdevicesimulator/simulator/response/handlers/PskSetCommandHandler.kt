// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskEqualityException
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskHashException
import org.gxf.crestdevicesimulator.simulator.response.psk.PskService
import org.springframework.stereotype.Service

/**
 * Command Handler for PSK SET downlink Confirms setting the new pre shared key (PSK) On success: "PSK:SET" URC will be
 * returned in the next message sent On failure: "PSK:DLER", "PSK:EQER" or "PSK:HSER" URC will be returned in the next
 * message sent
 */
@Service
class PskSetCommandHandler(val pskService: PskService) : CommandHandler {

    private val logger = KotlinLogging.logger {}
    private val commandRegex = "PSK:([a-zA-Z0-9]{16}):([a-zA-Z0-9]{64}):SET".toRegex()

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        if (canHandleCommand(command)) {
            try {
                handlePskSetCommand(command, simulatorState)
            } catch (ex: Exception) {
                handleFailure(command, simulatorState, ex)
            }
        }
    }

    private fun canHandleCommand(command: String) = commandRegex.matches(command)

    private fun handlePskSetCommand(command: String, simulatorState: SimulatorState) {
        logger.info { "Handling PSK SET command: $command" }
        pskService.verifyPendingKey(command)
        simulatorState.addUrc(URC_PSK_SUCCESS)
        simulatorState.addDownlink(DOWNLINK_PSK_SET)
    }

    private fun handleFailure(command: String, simulatorState: SimulatorState, ex: Exception) {
        logger.error(ex) { "Handling failure for PSK SET command: $command" }
        when (ex) {
            is InvalidPskHashException -> simulatorState.addUrc(URC_PSK_ERROR_HASH)
            is InvalidPskEqualityException -> simulatorState.addUrc(URC_PSK_ERROR_EQUALITY)
            else -> simulatorState.addUrc(URC_PSK_ERROR_DOWNLINK)
        }
        simulatorState.addDownlink(DOWNLINK_PSK_SET)
        pskService.setPendingKeyAsInvalid()
    }

    companion object {
        private const val URC_PSK_SUCCESS = "PSK:SET"
        private const val URC_PSK_ERROR_DOWNLINK = "PSK:DLER"
        private const val URC_PSK_ERROR_EQUALITY = "PSK:EQER"
        private const val URC_PSK_ERROR_HASH = "PSK:HSER"
        private const val DOWNLINK_PSK_SET = "PSK:################:SET"
    }
}
