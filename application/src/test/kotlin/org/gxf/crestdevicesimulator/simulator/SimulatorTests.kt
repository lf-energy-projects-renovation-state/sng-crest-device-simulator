package org.gxf.crestdevicesimulator.simulator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapResponse
import org.eclipse.californium.core.coap.Request
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.coap.CoapClientService
import org.gxf.crestdevicesimulator.simulator.response.ResponseHandler
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.util.ResourceUtils

@ExtendWith(MockitoExtension::class)
class SimulatorTests {

    @Mock
    private lateinit var simulatorProperties: SimulatorProperties

    @Mock
    private lateinit var coapClient: CoapClient

    @Mock
    private lateinit var coapClientService: CoapClientService

    @Mock
    private lateinit var responseHandler: ResponseHandler

    @InjectMocks
    private lateinit var simulator: Simulator

    @BeforeEach
    fun setup() {
        `when`(coapClient.advanced(any())).thenReturn(mock(CoapResponse::class.java))
        `when`(simulatorProperties.messagePath).thenReturn("test-file.json")
        `when`(coapClientService.createCoapClient()).thenReturn(coapClient)
    }


    @Test
    fun shouldSendInvalidCborWhenTheMessageTypeIsInvalidCbor() {
        `when`(simulatorProperties.produceValidCbor).thenReturn(false)
        val argument = ArgumentCaptor.forClass(Request::class.java)

        simulator.sendPostMessage()

        verify(coapClient).advanced(argument.capture())
        assertEquals(CborFactory.INVALID_CBOR_MESSAGE, argument.value.payloadString)
    }

    @Test
    fun shouldSendCborFromConfiguredJsonFileWhenTheMessageTypeIsCbor() {
        `when`(simulatorProperties.produceValidCbor).thenReturn(true)

        val fileToUse = ResourceUtils.getFile("classpath:test-file.json")
        val argument = ArgumentCaptor.forClass(Request::class.java)

        simulator.sendPostMessage()
        verify(coapClient).advanced(argument.capture())

        val expected = CBORMapper().writeValueAsBytes(ObjectMapper().readTree(fileToUse))
        assertArrayEquals(expected, argument.value.payload)
    }
}
