// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.coap

import java.net.InetSocketAddress
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.CoAP
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.elements.config.Configuration
import org.eclipse.californium.scandium.DTLSConnector
import org.eclipse.californium.scandium.MdcConnectionListener
import org.eclipse.californium.scandium.config.DtlsConnectorConfig
import org.eclipse.californium.scandium.dtls.ProtocolVersion
import org.gxf.crestdevicesimulator.configuration.AdvancedSingleIdentityPskStore
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.springframework.stereotype.Service

@Service
class CoapClientService(
    private val simulatorProperties: SimulatorProperties,
    private val advancedSingleIdentityPskStore: AdvancedSingleIdentityPskStore,
    private val configuration: Configuration
) {

    fun shutdownCoapClient(coapClient: CoapClient) {
        coapClient.endpoint.stop()
        coapClient.endpoint.destroy()
        coapClient.shutdown()
    }

    fun createCoapClient(): CoapClient {
        val uri = simulatorProperties.uri
        val coapClient = CoapClient(uri)
        if (uri.scheme == CoAP.COAP_SECURE_URI_SCHEME) {
            val endpoint =
                CoapEndpoint.Builder()
                    .setConfiguration(configuration)
                    .setConnector(createDtlsConnector(advancedSingleIdentityPskStore))
                    .build()
            coapClient.setEndpoint(endpoint)
        }
        return coapClient
    }

    private fun createDtlsConnector(advancedSingleIdentityPskStore: AdvancedSingleIdentityPskStore): DTLSConnector {
        val address = InetSocketAddress(0)
        val dtlsBuilder =
            DtlsConnectorConfig.builder(configuration)
                .setAddress(address)
                .setAdvancedPskStore(advancedSingleIdentityPskStore)
                .setConnectionListener(MdcConnectionListener())
                .setProtocolVersionForHelloVerifyRequests(ProtocolVersion.VERSION_DTLS_1_2)
                .build()
        return DTLSConnector(dtlsBuilder)
    }
}
