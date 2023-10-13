// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.configuration

import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.elements.config.Configuration
import org.eclipse.californium.scandium.DTLSConnector
import org.eclipse.californium.scandium.MdcConnectionListener
import org.eclipse.californium.scandium.config.DtlsConnectorConfig
import org.eclipse.californium.scandium.dtls.ProtocolVersion
import org.eclipse.californium.scandium.dtls.pskstore.AdvancedSinglePskStore
import org.springframework.context.annotation.Bean
import java.net.InetSocketAddress

@org.springframework.context.annotation.Configuration
class CoapClientConfiguration(private val configuration: Configuration, private val simulatorProperties: SimulatorProperties) {

    @Bean
    fun getClient(): CoapClient {
        val uri = this.getUri()
        val coapClient = CoapClient(uri)
        if (this.simulatorProperties.useDtls) {
            val dtlsConnector = this.createDtlsConnector()
            val endpoint = CoapEndpoint.Builder()
                    .setConfiguration(configuration)
                    .setConnector(dtlsConnector)
                    .build()
            coapClient.setEndpoint(endpoint)
        }
        return coapClient
    }

    private fun getUri(): String {
        with(this.simulatorProperties) {
            val protocol = if (useDtls) "coaps" else "coap"
            val host = if (localTesting) localHost else remoteHost
            val port = if (useDtls) dtlsPort else port
            val path = path
            return String.format("%s://%s:%d/%s", protocol, host, port, path)
        }
    }

    private fun createDtlsConnector(): DTLSConnector {
        val address = InetSocketAddress(0)
        val pskStore = createPskStore()
        val dtlsBuilder = DtlsConnectorConfig.builder(configuration)
                .setAddress(address)
                .setAdvancedPskStore(pskStore)
                .setConnectionListener(MdcConnectionListener())
                .setProtocolVersionForHelloVerifyRequests(ProtocolVersion.VERSION_DTLS_1_2)
                .build()
        return DTLSConnector(dtlsBuilder)
    }

    private fun createPskStore(): AdvancedSinglePskStore {
        return AdvancedSinglePskStore(this.simulatorProperties.pskIdentity, this.simulatorProperties.pskKey.toByteArray())
    }
}
