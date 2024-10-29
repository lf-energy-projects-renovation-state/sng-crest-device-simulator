// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import org.eclipse.californium.core.CoapResponse
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState

fun interface CommandHandler {
    /**
     * Handles the response sent by the device-service
     *
     * @param response Response sent by the device service
     * @param simulatorState State variables of the simulator
     * @return URC or empty string if nothing to report
     */
    fun handleResponse(response: CoapResponse, simulatorState: SimulatorState)
}
