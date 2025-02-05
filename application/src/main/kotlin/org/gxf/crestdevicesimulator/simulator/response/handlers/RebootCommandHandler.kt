// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.springframework.stereotype.Service

@Service
class RebootCommandHandler : CommandHandler {
    private val logger = KotlinLogging.logger {}

    override fun canHandleCommand(command: String) = command == CMD_REBOOT

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        require(canHandleCommand(command)) { "Reboot command handler can not handle command: $command" }

        handleRebootCommand(command, simulatorState)
    }

    fun handleRebootCommand(command: String, simulatorState: SimulatorState) {
        logger.info { "Handling reboot command: $command" }
        simulatorState.addUrc("INIT")
        simulatorState.addUrc("WDR")
        simulatorState.addDownlink(CMD_REBOOT)
    }

    companion object {
        private const val CMD_REBOOT = "CMD:REBOOT"
    }
}
