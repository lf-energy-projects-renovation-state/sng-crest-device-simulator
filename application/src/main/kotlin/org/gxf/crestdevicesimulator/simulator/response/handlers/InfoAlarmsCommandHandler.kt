// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.simulator.data.entity.AlarmThresholdValues
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidCommandException

/**
 * Command Handler for INFO:AL# and INFO:ALARM downlinks.
 *
 * Gets the alarm thresholds for the specified alarm or for all alarms
 * * On success: a downlink containing the alarm thresholds will be returned in the next message sent
 * * On failure: "INFO:DLER" URC will be returned in the next message sent
 */
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
            simulatorState.addDownlink(command.inQuotes())
            if (isForSpecificAlarm(command)) {
                val channel: Int = getChannelFromCommand(command)
                val thresholds =
                    checkNotNull(simulatorState.getAlarmThresholds(channel)) {
                        "Alarm thresholds for channel $channel not present."
                    }
                simulatorState.addDownlink(thresholds.toJsonString().inBraces())
            } else {
                simulatorState.addDownlink(simulatorState.getAlarmThresholds().values.toJsonString())
            }
        } catch (ex: Exception) {
            throw InvalidCommandException("Invalid info alarm command", ex)
        }
    }

    private fun handleFailure(command: String, simulatorState: SimulatorState) {
        logger.warn { "Handling failure for info alarm command: $command" }
        simulatorState.addUrc("INFO:DLER")
        simulatorState.addDownlink(command.inQuotes())
    }

    private fun isForSpecificAlarm(command: String) = !isForAllAlarms(command)

    private fun isForAllAlarms(command: String) = command.contains("ALARM")

    private fun getChannelFromCommand(command: String) = command.substringAfter("AL").substringBefore(":").toInt()

    private fun MutableCollection<AlarmThresholdValues>.toJsonString() =
        this.joinToString(",") { it.toJsonString() }.inBraces()

    private fun String.inQuotes() = "\"$this\""

    private fun String.inBraces() = "{$this}"

    private fun AlarmThresholdValues.toJsonString() =
        "\"AL${this.channel}\":[${this.veryLow},${this.low},${this.high},${this.veryHigh},${this.hysteresis}]"
}
