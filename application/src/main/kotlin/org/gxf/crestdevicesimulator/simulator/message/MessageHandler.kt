package org.gxf.crestdevicesimulator.simulator.message

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
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
    private val pskCommandHandler: PskCommandHandler,
    private val mapper: ObjectMapper
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val URC_FIELD = "URC"
        private const val URC_PSK_SUCCESS = "PSK:SET"
        private const val URC_PSK_ERROR = "PSK:EQER"
        private const val DL_FIELD = "DL"
    }

    fun sendMessage(jsonNode: JsonNode) {
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
            logger.info { "RESPONSE: ${String(response.payload)}" }
            handleResponse(response)
        } catch (e: ConnectorException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (coapClient != null) coapClientService.shutdownCoapClient(coapClient)
        }
    }

    private fun handleResponse(response: CoapResponse) {
        val payload = String(response.payload)

        if (PskExtractor.hasPskCommand(payload)) {
            try {
                pskCommandHandler.handlePskChange(payload)
                sendSuccessMessage(payload)
                pskCommandHandler.changeActiveKey()
            } catch (e: Exception) {
                logger.error(e) { "PSK change error, send failure message" }
                sendFailureMessage(payload)
                pskCommandHandler.setPendingKeyAsInvalid()
            }
        }
    }

    private fun sendSuccessMessage(pskCommand: String) {
        logger.info { "Sending success message for command $pskCommand" }
        val messageJsonNode =
            ObjectMapper().readTree(ClassPathResource(simulatorProperties.successMessagePath).file)
        val message = updatePskCommandInMessage(messageJsonNode, URC_PSK_SUCCESS, pskCommand)
        sendMessage(message)
    }

    private fun sendFailureMessage(pskCommand: String) {
        logger.info { "Sending failure message for command $pskCommand" }
        val messageJsonNode =
            ObjectMapper().readTree(ClassPathResource(simulatorProperties.failureMessagePath).file)
        val message = updatePskCommandInMessage(messageJsonNode, URC_PSK_ERROR, pskCommand)
        sendMessage(message)
    }

    private fun updatePskCommandInMessage(
        message: JsonNode,
        urc: String,
        receivedCommand: String
    ): JsonNode {
        val newMessage = message as ObjectNode
        val urcList = listOf(
            TextNode(urc),
            ObjectNode(JsonNodeFactory.instance, mapOf(DL_FIELD to TextNode(receivedCommand)))
        )
        val array = mapper.valueToTree<ArrayNode>(urcList)
        newMessage.replace(URC_FIELD, array)
        return newMessage
    }
}
