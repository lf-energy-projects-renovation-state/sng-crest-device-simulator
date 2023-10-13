package org.gxf.crestdevicesimulator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import org.awaitility.Awaitility
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.elements.config.Configuration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import java.time.Duration


@SpringBootTest
class SimulatorIntegrationTest {
    @Value("\${simulator.config.port}")
    private lateinit var port: String

    private lateinit var coapServer: CoapServer
    private val coapResourceStub = CoapResourceStub()
    private val expectedJsonNode = ObjectMapper().readTree(ClassPathResource("messages/kod-message.json").file)

    @BeforeEach
    fun setup() {
        coapServer = CoapServer(Configuration.getStandard())
        coapServer.addEndpoint(CoapServerHelpers.createEndpoint(Configuration.getStandard(), port.toInt()))
        coapServer.add(coapResourceStub)
        coapServer.start()
    }

    @Test
    fun shouldSendCoapRequestToConfiguredEndpoint() {
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted {
            val jsonNodeStub = CBORMapper().readTree(coapResourceStub.lastRequestPayload)
            Assertions.assertEquals(expectedJsonNode, jsonNodeStub)
        }
    }
}