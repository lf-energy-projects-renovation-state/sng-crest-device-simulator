// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.message.MessageHandler
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class Simulator(private val simulatorProperties: SimulatorProperties, private val messageHandler: MessageHandler) :
    CommandLineRunner {

    private val logger = KotlinLogging.logger {}

    override fun run(args: Array<String>) {
        logger.info { "Simulator config started with config: $simulatorProperties" }
        // This simulates the device trying to send as many messages as possible before the
        // capacitor depletes
        val maxNumberOfMessagesInBatch = 5

        // Start infinite message sending loop in separate thread
        // This ensures Spring Boot can complete startup and doesn't block on the infinite loop
        Thread.ofVirtual().start {
            val simulatorState = SimulatorState()
            var numberOfMessagesInBatch = 0
            while (true) {
                logger.info { "Sending device message" }
                val immediateResponseRequested = messageHandler.sendMessage(simulatorState)

                if (!immediateResponseRequested || numberOfMessagesInBatch++ >= maxNumberOfMessagesInBatch) {
                    logger.info { "Sleeping for: ${simulatorProperties.sleepDuration}" }
                    Thread.sleep(simulatorProperties.sleepDuration)
                    numberOfMessagesInBatch = 0
                }
            }
        }
    }
}
