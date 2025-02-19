// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidCommandException
import org.springframework.stereotype.Service

/**
 * Command Handler for INFO:AL# and INFO:ALARM downlinks.
 *
 * Gets the alarm thresholds for the specified alarm or for all alarms
 * * On success: a downlink containing the alarm thresholds will be returned in the next message sent
 * * On failure: "INFO:DLER" URC will be returned in the next message sent
 */
@Service
class InfoAlarmsCommandHandler : CommandHandler {
    private val logger = KotlinLogging.logger {}
    private val commandRegex: Regex = "INFO:AL([2-7]|ARMS)".toRegex()

    override fun canHandleCommand(command: String) = commandRegex.matches(command)

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        require(canHandleCommand(command)) { "Info alarm command handler can not handle command: $command" }
        try {
            handleInfoAlarmCommand(command, simulatorState)
        } catch (_: InvalidCommandException) {
            handleFailure(command, simulatorState)
        }
    }

    private fun handleInfoAlarmCommand(command: String, simulatorState: SimulatorState) {
        logger.info { "Handling info alarm command: $command" }
        try {
            simulatorState.addDownlink(command)
            if (isForSpecificAlarm(command)) {
                val channel: Int = getChannelFromCommand(command)
                val thresholds =
                    checkNotNull(simulatorState.getAlarmThresholds(channel)) {
                        "Alarm thresholds for channel $channel not present."
                    }
                simulatorState.addUrc(
                    mapOf(
                        "AL${thresholds.channel}" to
                            listOf(
                                thresholds.veryLow,
                                thresholds.low,
                                thresholds.high,
                                thresholds.veryHigh,
                                thresholds.hysteresis,
                            )
                    )
                )
            } else {
                simulatorState.addUrc(
                    simulatorState.alarmThresholds.values.associate {
                        "AL${it.channel}" to listOf(it.veryLow, it.low, it.high, it.veryHigh, it.hysteresis)
                    }
                )
            }
        } catch (ex: Exception) {
            throw InvalidCommandException("Invalid info alarm command", ex)
        }
    }

    private fun handleFailure(command: String, simulatorState: SimulatorState) {
        logger.warn { "Handling failure for info alarm command: $command" }
        simulatorState.addUrc("INFO:DLER")
        simulatorState.addDownlink(command)
    }

    private fun isForSpecificAlarm(command: String) = !isForAllAlarms(command)

    private fun isForAllAlarms(command: String) = command.contains("ALARM")

    private fun getChannelFromCommand(command: String) = command.substringAfter("AL").substringBefore(":").toInt()
}
