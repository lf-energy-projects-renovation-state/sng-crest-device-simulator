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
class InfoAlarmCommandHandler : CommandHandler {
    private val logger = KotlinLogging.logger {}
    private val commandRegex: Regex = "INFO:AL([2-7]|ARM)".toRegex()

    override fun canHandleCommand(command: String) = commandRegex.matches(command)

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        require(canHandleCommand(command)) { "Info alarm command handler can not handle command: $command" }
        try {
            handleInfoAlarmCommand(command, simulatorState)
        } catch (ex: InvalidCommandException) {
            handleFailure(command, simulatorState)
        }
    }

    private fun handleInfoAlarmCommand(command: String, simulatorState: SimulatorState) {
        logger.info { "Handling info alarm command: $command" }
        try {
            simulatorState.addDownlink(command.betweenQuotes())
            if (isForSpecificAlarm(command)) {
                val index: Int = getIndexFromCommand(command)
                simulatorState.addDownlink(simulatorState.getAlarmThresholds(index)!!.toJsonString().betweenBraces())
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
        simulatorState.addDownlink(command.betweenQuotes())
    }

    private fun isForSpecificAlarm(command: String) = !isForAllAlarms(command)

    private fun isForAllAlarms(command: String) = command.contains("ALARM")

    private fun getIndexFromCommand(command: String) = command.substringAfter("AL").substringBefore(":").toInt()

    private fun MutableCollection<AlarmThresholdValues>.toJsonString(): String {
        val values = this.joinToString(",") { it.toJsonString() }
        return "{$values}"
    }

    private fun String.betweenQuotes() = "\"$this\""

    private fun String.betweenBraces() = "{$this}"

    private fun AlarmThresholdValues.toJsonString() =
        "\"AL${this.channel}\":[${this.veryLow},${this.low},${this.high},${this.veryHigh},${this.hysteresis}]"
}
