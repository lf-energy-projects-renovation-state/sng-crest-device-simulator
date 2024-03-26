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
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class Simulator(
    private val simulatorProperties: SimulatorProperties,
    private val messageHandler: MessageHandler,
    private val mapper: ObjectMapper
) : CommandLineRunner {

    private val logger = KotlinLogging.logger {}

    override fun run(args: Array<String>) {
        logger.info { "Simulator config started with config: $simulatorProperties" }
        val message: JsonNode = createMessage(simulatorProperties.scheduledMessage)

        // Start infinite message sending loop in separate thread
        // This ensures Spring Boot can complete startup and doesn't block on the infinite loop
        Thread.ofVirtual().start {
            while (true) {
                logger.info { "Sending scheduled alarm message" }
                messageHandler.sendMessage(message)

                logger.info { "Sleeping for: ${simulatorProperties.sleepDuration}" }
                Thread.sleep(simulatorProperties.sleepDuration)
            }
        }
    }

    fun createMessage(resource: Resource): JsonNode =
        mapper.readTree(resource.inputStream)
}
