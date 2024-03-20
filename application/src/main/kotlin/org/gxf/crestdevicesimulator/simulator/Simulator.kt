// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.message.MessageHandler
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class Simulator(
    private val simulatorProperties: SimulatorProperties,
    private val messageHandler: MessageHandler
) {

    private val logger = KotlinLogging.logger {}

    init {
        logger.info { "Simulator config started with config: $simulatorProperties" }
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 0)
    fun sendScheduledMessage() {
            logger.info { "Sending scheduled alarm message " }
            val message =
                ObjectMapper().readTree(ClassPathResource(simulatorProperties.scheduledMessagePath).file)
            messageHandler.sendMessage(message)
    }
}
