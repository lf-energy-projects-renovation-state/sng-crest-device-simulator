// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import java.net.URI
import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.elements.config.Configuration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource

@SpringBootTest
class SimulatorIntegrationTest {

    @Value("\${simulator.config.uri}") private lateinit var uri: URI

    private val mapper = ObjectMapper()
    private lateinit var coapServer: CoapServer
    private val coapResourceStub = CoapResourceStub()
    private val expectedJsonNode = mapper.readTree(ClassPathResource("messages/kod-message.json").file)

    @BeforeEach
    fun setup() {
        coapServer = CoapServer(Configuration.getStandard())
        coapServer.addEndpoint(CoapServerHelpers.createEndpoint(Configuration.getStandard(), uri.port))
        coapServer.add(coapResourceStub)
        coapServer.start()
    }

    @Test
    fun shouldSendCoapRequestToConfiguredEndpoint() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted {
            val jsonNodeStub = CBORMapper().readTree(coapResourceStub.lastRequestPayload)
            assertThat(jsonNodeStub).isEqualTo(expectedJsonNode)
        }
    }
}
