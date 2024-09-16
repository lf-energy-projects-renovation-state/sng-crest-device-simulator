// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator

import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.server.resources.CoapExchange

class CoapResourceStub : CoapResource("coap-path") {
    var lastRequestPayload = byteArrayOf()

    override fun handlePOST(coapExchange: CoapExchange) {
        lastRequestPayload = coapExchange.requestPayload
    }
}
