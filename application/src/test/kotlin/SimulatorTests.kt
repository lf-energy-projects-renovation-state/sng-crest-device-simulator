import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapResponse
import org.eclipse.californium.core.coap.Request
import org.gxf.generiekemeldersimulator.configuration.SimulatorProperties
import org.gxf.generiekemeldersimulator.simulator.CborFactory
import org.gxf.generiekemeldersimulator.simulator.Simulator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.util.ResourceUtils

@ExtendWith(MockitoExtension::class)
class SimulatorTests {

    @Mock
    private lateinit var simulatorProperties: SimulatorProperties

    @Mock
    private lateinit var coapClient: CoapClient

    @InjectMocks
    private lateinit var simulator: Simulator

    @BeforeEach
    fun setup() {
        Mockito.`when`(coapClient.advanced(Mockito.any())).thenReturn(Mockito.mock(CoapResponse::class.java))
        Mockito.`when`(simulatorProperties.messagePath).thenReturn("test-file.json")
    }


    @Test
    fun shouldSendInvalidCborWhenTheMessageTypeIsInvalidCbor() {
        Mockito.`when`(simulatorProperties.produceValidCbor).thenReturn(false)
        val argument = ArgumentCaptor.forClass(Request::class.java)

        simulator.sendPostMessage()

        Mockito.verify(coapClient).advanced(argument.capture())
        Assertions.assertEquals(CborFactory.invalidCborMessage, argument.value.payloadString)
    }

    @Test
    fun shouldSendCborFromConfiguredJsonFileWhenTheMessageTypeIsCbor() {
        Mockito.`when`(simulatorProperties.produceValidCbor).thenReturn(true)

        val fileToUse = ResourceUtils.getFile("classpath:test-file.json")
        val argument = ArgumentCaptor.forClass(Request::class.java)

        simulator.sendPostMessage()
        Mockito.verify(coapClient).advanced(argument.capture())

        val expected = CBORMapper().writeValueAsBytes(ObjectMapper().readTree(fileToUse))
        Assertions.assertArrayEquals(expected, argument.value.payload)
    }
}