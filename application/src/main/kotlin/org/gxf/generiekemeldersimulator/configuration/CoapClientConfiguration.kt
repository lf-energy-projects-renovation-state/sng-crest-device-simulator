// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.generiekemeldersimulator.configuration

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
            val dtlsConnector: DTLSConnector = this.createDtlsConnector(configuration, 0)
            val endpoint = CoapEndpoint.Builder()
                    .setConfiguration(configuration)
                    .setConnector(dtlsConnector)
                    .build()
            coapClient.setEndpoint(endpoint)
        }
        return coapClient
    }

    fun getUri(): String {
        val protocol = if (this.simulatorProperties.useDtls) "coaps" else "coap"
        val host: String = if (this.simulatorProperties.localTesting) this.simulatorProperties.localHost else this.simulatorProperties.remoteHost
        val port: Int = if (this.simulatorProperties.useDtls) this.simulatorProperties.dtlsPort else this.simulatorProperties.port
        val path: String = this.simulatorProperties.path
        return String.format("%s://%s:%d/%s", protocol, host, port, path)
    }

    private fun createDtlsConnector(config: Configuration, port: Int): DTLSConnector {
        val address = InetSocketAddress(port)
        val pskStore = createPskStore()
        val dtlsBuilder = DtlsConnectorConfig.builder(config)
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