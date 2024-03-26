// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

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
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.CborFactory
import org.gxf.crestdevicesimulator.simulator.coap.CoapClientService
import org.gxf.crestdevicesimulator.simulator.response.PskExtractor
import org.gxf.crestdevicesimulator.simulator.response.command.PskCommandHandler
import org.springframework.stereotype.Service

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
        val request = createRequest(jsonNode)
        logger.info { "Sending request: $request" }

        var coapClient: CoapClient? = null

        try {
            coapClient = coapClientService.createCoapClient()
            val response = coapClient.advanced(request)
            logger.info { "Received Response: ${response.payload.decodeToString()}" }
            handleResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (coapClient != null) coapClientService.shutdownCoapClient(coapClient)
        }
    }

    fun createRequest(jsonNode: JsonNode): Request{
        val payload =
            if (simulatorProperties.produceValidCbor) CborFactory.createValidCbor(jsonNode)
            else CborFactory.createInvalidCbor()

        return Request.newPost()
            .apply {
                options.setContentFormat(MediaTypeRegistry.APPLICATION_CBOR)
            }.setPayload(payload)
    }

    private fun handleResponse(response: CoapResponse) {
        val payload = String(response.payload)

        if (PskExtractor.hasPskSetCommand(payload)) {
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
            mapper.readTree(simulatorProperties.successMessage.inputStream)
        val message = updatePskCommandInMessage(messageJsonNode, URC_PSK_SUCCESS, pskCommand)
        sendMessage(message)
    }

    private fun sendFailureMessage(pskCommand: String) {
        logger.warn { "Sending failure message for command $pskCommand" }
        val messageJsonNode =
            mapper.readTree(simulatorProperties.failureMessage.inputStream)
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
