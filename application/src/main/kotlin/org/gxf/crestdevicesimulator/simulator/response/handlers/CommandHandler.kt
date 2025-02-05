// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState

interface CommandHandler {

    /**
     * Checks if a command handler can handle a command sent as downlink by the device service
     *
     * @param command Command sent by the device service
     * @return a boolean indicating if the command handler is able to handle the command
     */
    fun canHandleCommand(command: String): Boolean

    /**
     * Handles a command sent as downlink by the device-service
     *
     * @param command Command sent by the device service
     * @param simulatorState State variables of the simulator
     */
    fun handleCommand(command: String, simulatorState: SimulatorState)
}
