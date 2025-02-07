// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdevicesimulator.simulator.data.entity.AlarmThresholdValues
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidCommandException
import org.springframework.stereotype.Service

/**
 * Command Handler for AL# downlink (where # is a number between 2 and 7) Sets the alarm thresholds for the specified
 * alarm On success: "AL#:SET" URC will be returned in the next message sent On failure: "AL#:DLER" URC will be returned
 * in the next message sent
 */
@Service
class AlarmCommandHandler : CommandHandler {
    private val logger = KotlinLogging.logger {}
    private val commandRegex: Regex =
        "^AL(?<channel>[2-7]):(?<veryLow>-?\\d+),(?<low>-?\\d+),(?<high>-?\\d+),(?<veryHigh>-?\\d+),(?<hysteresis>-?\\d+)$"
            .toRegex()

    override fun canHandleCommand(command: String) = commandRegex.matches(command)

    override fun handleCommand(command: String, simulatorState: SimulatorState) {
        require(canHandleCommand(command)) { "Alarm command handler can not handle command: $command" }

        try {
            handleAlarmCommand(command, simulatorState)
        } catch (ex: Exception) {
            handleFailure(command, simulatorState)
        }
    }

    private fun handleAlarmCommand(command: String, simulatorState: SimulatorState) {
        logger.info { "Handling alarm command: $command" }
        try {
            val alarmThresholds = parseAlarmThresholdValues(command)
            simulatorState.addAlarmThresholds(alarmThresholds)
            simulatorState.addUrc("AL${alarmThresholds.channel}:SET")
            simulatorState.addDownlink(command)
        } catch (ex: Exception) {
            throw InvalidCommandException("Invalid alarm command", ex)
        }
    }

    private fun handleFailure(command: String, simulatorState: SimulatorState) {
        logger.warn { "Handling failure for alarm command: $command" }
        val alarm = command.split(":").first()
        simulatorState.addUrc("$alarm:DLER")
        simulatorState.addDownlink(command)
    }

    private fun parseAlarmThresholdValues(command: String): AlarmThresholdValues {
        val regexGroups = commandRegex.findAll(command).first().groups
        return AlarmThresholdValues(
            regexGroups["channel"]!!.value.toInt(),
            regexGroups["veryLow"]!!.value.toInt(),
            regexGroups["low"]!!.value.toInt(),
            regexGroups["high"]!!.value.toInt(),
            regexGroups["veryHigh"]!!.value.toInt(),
            regexGroups["hysteresis"]!!.value.toInt(),
        )
    }
}
