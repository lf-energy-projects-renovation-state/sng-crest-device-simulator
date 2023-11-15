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
import org.gxf.crestdevicesimulator.simulator.data.entity.Psk
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.springframework.context.annotation.Bean
import java.net.InetSocketAddress

@org.springframework.context.annotation.Configuration
class CoapClientConfiguration(private val configuration: Configuration,
                              private val simulatorProperties: SimulatorProperties,
                              private val pskRepository: PskRepository) {

    @Bean
    fun getClient(dtlsConnector: DTLSConnector): CoapClient {
        val uri = this.getUri()
        val coapClient = CoapClient(uri)
        if (this.simulatorProperties.useDtls) {
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

    @Bean
    fun dtlsConnector(advancedSingleIdentityPskStore: AdvancedSingleIdentityPskStore): DTLSConnector {
        val address = InetSocketAddress(0)
        val dtlsBuilder = DtlsConnectorConfig.builder(configuration)
                .setAddress(address)
                .setAdvancedPskStore(advancedSingleIdentityPskStore)
                .setConnectionListener(MdcConnectionListener())
                .setProtocolVersionForHelloVerifyRequests(ProtocolVersion.VERSION_DTLS_1_2)
                .build()
        return DTLSConnector(dtlsBuilder)
    }

    @Bean
    fun pskStore(): AdvancedSingleIdentityPskStore {
        val store = AdvancedSingleIdentityPskStore(simulatorProperties.pskIdentity)
        val savedKey = pskRepository.findById(simulatorProperties.pskIdentity)

        if (savedKey.isEmpty) {
            val initialPsk = Psk(simulatorProperties.pskIdentity, simulatorProperties.pskKey)
            pskRepository.save(initialPsk)
            store.key = simulatorProperties.pskKey
        } else {
            store.key = savedKey.get().key
        }

        return store
    }
}
