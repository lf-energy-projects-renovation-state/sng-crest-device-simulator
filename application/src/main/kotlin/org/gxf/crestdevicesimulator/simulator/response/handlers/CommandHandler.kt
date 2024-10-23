// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import org.eclipse.californium.core.CoapResponse
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties

fun interface CommandHandler {
    /**
     * Handles the response sent by the device-service
     *
     * @param response Response sent by the device service
     * @return URC or empty string if nothing to report
     */
    fun handleResponse(response: CoapResponse, simulatorProperties: SimulatorProperties): String
}
