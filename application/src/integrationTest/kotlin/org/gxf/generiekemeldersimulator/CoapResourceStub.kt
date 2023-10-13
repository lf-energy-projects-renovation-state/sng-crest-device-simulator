package org.gxf.generiekemeldersimulator

import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.server.resources.CoapExchange

class CoapResourceStub : CoapResource("coap-path") {
    var lastRequestPayload = byteArrayOf()

    override fun handlePOST(coapExchange: CoapExchange) {
        lastRequestPayload = coapExchange.requestPayload
    }
}