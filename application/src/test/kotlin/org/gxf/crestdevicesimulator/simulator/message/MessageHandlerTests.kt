package org.gxf.crestdevicesimulator.simulator.message

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapResponse
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.CborFactory
import org.gxf.crestdevicesimulator.simulator.coap.CoapClientService
import org.gxf.crestdevicesimulator.simulator.response.command.PskCommandHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.util.ResourceUtils
import kotlin.math.exp

@ExtendWith(MockitoExtension::class)
class MessageHandlerTests {
    @Spy
    private val mapper = ObjectMapper()

    @Mock
    private lateinit var simulatorProperties: SimulatorProperties

    @Mock
    private lateinit var coapClient: CoapClient

    @Mock
    private lateinit var coapClientService: CoapClientService

    @Mock
    private lateinit var pskCommandHandler: PskCommandHandler

    @InjectMocks
    private lateinit var messageHandler: MessageHandler

    @Test
    fun shouldSendInvalidCborWhenTheMessageTypeIsInvalidCbor() {
        `when`(simulatorProperties.produceValidCbor).thenReturn(false)
        val message = mapper.readTree(testFile())

        val request = messageHandler.createRequest(message)

        assertThat(request.payloadString).isEqualTo(CborFactory.INVALID_CBOR_MESSAGE)
    }

    @Test
    fun shouldSendCborFromConfiguredJsonFileWhenTheMessageTypeIsCbor() {
        `when`(simulatorProperties.produceValidCbor).thenReturn(true)
        val message = mapper.readTree(testFile())

        val request = messageHandler.createRequest(message)
        val expected = CBORMapper().writeValueAsBytes(message)
        assertThat(request.payload).containsExactly(expected.toTypedArray())
    }

    private fun testFile() =
        ResourceUtils.getFile("classpath:test-file.json")
}
