// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator

import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.core.network.Endpoint
import org.eclipse.californium.elements.config.Configuration

object CoapServerHelpers {
    fun createEndpoint(config: Configuration, port: Int): Endpoint {
        return CoapEndpoint.builder().setConfiguration(config).setPort(port).build()
    }
}
