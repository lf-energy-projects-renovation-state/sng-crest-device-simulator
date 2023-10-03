// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.generiekemeldersimulator.simulator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import mu.KotlinLogging
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.eclipse.californium.core.coap.Request
import org.eclipse.californium.elements.exception.ConnectorException
import org.gxf.generiekemeldersimulator.configuration.SimulatorProperties
import org.springframework.core.io.ResourceLoader
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class Simulator(private val simulatorProperties: SimulatorProperties,
                private val coapClient: CoapClient,
                private val resourceLoader: ResourceLoader) {

    companion object {
        val logger = KotlinLogging.logger {}
    }

    init {
        logger.info("Simulator config started with config: $simulatorProperties")
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 0)
    fun sendPostMessage() {
        logger.info("SEND POST REQUEST")

        val jsonMessage = JsonMapper().readTree(resourceLoader.getResource("classpath:${simulatorProperties.messagePath}").file)

        val request =
                Request.newPost()
                        .apply {
                            options.setContentFormat(MediaTypeRegistry.APPLICATION_CBOR)
                        }.setPayload((getPayloadAsBytes(jsonMessage)))

        logger.info("SEND REQUEST $request")

        request(request)
    }

    private fun getPayloadAsBytes(jsonMessage: JsonNode): ByteArray {
        return when (simulatorProperties.cborMessageType) {
            CborMessageType.CBOR -> CBORMapper().writeValueAsBytes(jsonMessage)
            CborMessageType.INVALID_CBOR -> simulatorProperties.invalidCbor.toByteArray()
        }
    }

    private fun request(request: Request) {
        try {
            val response = coapClient.advanced(request)
            logger.info("RESPONSE $response")
        } catch (e: ConnectorException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}