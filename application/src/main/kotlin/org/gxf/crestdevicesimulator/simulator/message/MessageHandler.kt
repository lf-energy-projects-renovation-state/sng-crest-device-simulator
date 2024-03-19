package org.gxf.crestdevicesimulator.simulator.message

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapResponse
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.eclipse.californium.core.coap.Request
import org.eclipse.californium.elements.exception.ConnectorException
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.CborFactory
import org.gxf.crestdevicesimulator.simulator.coap.CoapClientService
import org.gxf.crestdevicesimulator.simulator.response.PskExtractor
import org.gxf.crestdevicesimulator.simulator.response.command.PskCommandHandler
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class MessageHandler(
    private val coapClientService: CoapClientService,
    private val simulatorProperties: SimulatorProperties,
    private val pskCommandHandler: PskCommandHandler
) {
    private val logger = KotlinLogging.logger {}

    fun sendMessage(messagePath: String) {
        val jsonNode = ObjectMapper().readTree(ClassPathResource(messagePath).file)
        val payload =
            if (simulatorProperties.produceValidCbor) CborFactory.createValidCbor(jsonNode) else CborFactory.createInvalidCbor()
        val request =
            Request.newPost()
                .apply {
                    options.setContentFormat(MediaTypeRegistry.APPLICATION_CBOR)
                }.setPayload(payload)

        logger.info { "SEND REQUEST $request" }

        request(request)
    }

    private fun request(request: Request) {
        var coapClient: CoapClient? = null
        try {
            coapClient = coapClientService.createCoapClient()
            val response = coapClient.advanced(request)
            handleResponse(response)
            logger.info { "RESPONSE $response" }
        } catch (e: ConnectorException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (coapClient != null) coapClientService.shutdownCoapClient(coapClient)
        }
    }

    private fun handleResponse(response: CoapResponse) {
        val body = String(response.payload)

        if (PskExtractor.hasPskCommand(body)) {
            try {
                pskCommandHandler.handlePskChange(body)
                sendSuccessMessage()
                pskCommandHandler.changeActiveKey()
            } else {
                sendFailureMessage()
                pskCommandHandler.setPendingKeyAsInvalid()
            }
        }

        readyForNewMessage = true
    }

    private fun sendSuccessMessage() {
        sendMessage(simulatorProperties.successMessagePath)
    }

    private fun sendFailureMessage() {
        sendMessage(simulatorProperties.failureMessagePath)
    }
}
