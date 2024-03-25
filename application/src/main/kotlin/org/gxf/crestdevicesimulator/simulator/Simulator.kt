// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.message.MessageHandler
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class Simulator(
    private val simulatorProperties: SimulatorProperties,
    private val messageHandler: MessageHandler,
    private val mapper: ObjectMapper
) : CommandLineRunner {

    private val logger = KotlinLogging.logger {}

    override fun run(vararg args: String?) {
        logger.info { "Simulator config started with config: $simulatorProperties" }

        val message: JsonNode = createMessage(simulatorProperties.scheduledMessage)
        while (true) {
            sendMessage(message)
        }
    }

    fun sendMessage(message: JsonNode) {
        logger.info { "Sending scheduled alarm message" }
        messageHandler.sendMessage(message)
        logger.info { "Sleeping for: ${simulatorProperties.sleepDuration}" }
        Thread.sleep(simulatorProperties.sleepDuration)
    }

    fun createMessage(resource: Resource): JsonNode =
        mapper.readTree(resource.inputStream)
}
