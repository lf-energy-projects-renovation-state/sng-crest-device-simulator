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
        logger.info { "Sending PSK set success message " }
        val messageJsonNode =
            ObjectMapper().readTree(ClassPathResource(simulatorProperties.successMessagePath).file)
        val message = updatePskCommandInMessage(messageJsonNode, pskCommand)
        sendMessage(message)
    }

    private fun sendFailureMessage(pskCommand: String) {
        logger.info { "Sending PSK set failure message " }
        val messageJsonNode =
            ObjectMapper().readTree(ClassPathResource(simulatorProperties.failureMessagePath).file)
        val pskErrorMessage = pskCommand.replace("SET", "EQER")
        val message = updatePskCommandInMessage(messageJsonNode, pskErrorMessage)
        sendMessage(message)
    }

    private fun updatePskCommandInMessage(jsonNode: JsonNode, pskCommand: String): JsonNode {
        val newMessage = jsonNode as ObjectNode
        val urcList = listOf(
            TextNode(pskCommand),
            ObjectNode(JsonNodeFactory.instance, mapOf("DL" to TextNode("0")))
        )
        val array = mapper.valueToTree<ArrayNode>(urcList)
        newMessage.replace("URC", array)
        return newMessage
    }
}
