package org.gxf.crestdevicesimulator.simulator.coap

import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.elements.config.Configuration
import org.eclipse.californium.scandium.DTLSConnector
import org.eclipse.californium.scandium.MdcConnectionListener
import org.eclipse.californium.scandium.config.DtlsConnectorConfig
import org.eclipse.californium.scandium.dtls.ProtocolVersion
import org.gxf.crestdevicesimulator.configuration.AdvancedSingleIdentityPskStore
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.springframework.stereotype.Service
import java.net.InetSocketAddress

@Service
class CoapClientService(
        private val simulatorProperties: SimulatorProperties,
        private val advancedSingleIdentityPskStore: AdvancedSingleIdentityPskStore,
        private val configuration: Configuration) {

    fun shutdownCoapClient(coapClient: CoapClient) {
        coapClient.endpoint.stop()
        coapClient.endpoint.destroy()
        coapClient.shutdown()
    }

    fun createCoapClient(): CoapClient {
        val uri = this.getUri()
        val coapClient = CoapClient(uri)
        if (this.simulatorProperties.useDtls) {
            val endpoint = CoapEndpoint.Builder()
                    .setConfiguration(configuration)
                    .setConnector(dtlsConnector(advancedSingleIdentityPskStore))
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

    private fun dtlsConnector(advancedSingleIdentityPskStore: AdvancedSingleIdentityPskStore): DTLSConnector {
        val address = InetSocketAddress(0)
        val dtlsBuilder = DtlsConnectorConfig.builder(configuration)
                .setAddress(address)
                .setAdvancedPskStore(advancedSingleIdentityPskStore)
                .setConnectionListener(MdcConnectionListener())
                .setProtocolVersionForHelloVerifyRequests(ProtocolVersion.VERSION_DTLS_1_2)
                .build()
        return DTLSConnector(dtlsBuilder)
    }
}
