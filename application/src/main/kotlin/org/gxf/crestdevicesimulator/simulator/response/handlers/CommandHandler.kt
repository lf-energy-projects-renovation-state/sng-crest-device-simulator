// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState

fun interface CommandHandler {
    /**
     * Handles the response sent by the device-service
     *
     * @param command Command sent as downlink by the device service
     * @param simulatorState State variables of the simulator
     */
    fun handleCommand(command: String, simulatorState: SimulatorState)
}
