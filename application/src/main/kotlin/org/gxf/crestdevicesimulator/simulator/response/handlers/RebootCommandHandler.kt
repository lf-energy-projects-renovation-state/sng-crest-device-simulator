// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.springframework.stereotype.Service

@Service
class RebootCommandHandler : CommandHandler {

    private val commandString = "CMD:REBOOT"

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        if (canHandleCommand(command)) {
            handleRebootCommand(simulatorState)
        }
    }

    private fun canHandleCommand(command: String) = command.contains("CMD:REBOOT")

    private fun handleRebootCommand(simulatorState: SimulatorState) {
        simulatorState.addUrc("INIT")
        simulatorState.addUrc("WDR")
        simulatorState.addDownlink("CMD:REBOOT")
    }
}
