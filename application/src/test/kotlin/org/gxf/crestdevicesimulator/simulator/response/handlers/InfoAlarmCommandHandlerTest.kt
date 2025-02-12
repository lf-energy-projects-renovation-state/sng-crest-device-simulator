// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.gxf.crestdevicesimulator.simulator.data.entity.AlarmThresholdValues
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class InfoAlarmCommandHandlerTest {
    private val commandHandler = InfoAlarmCommandHandler()

    private lateinit var simulatorState: SimulatorState

    @BeforeEach
    fun setUp() {
        simulatorState = SimulatorState(urcs = mutableListOf())
    }

    @ParameterizedTest
    @ValueSource(strings = ["INFO:ALARMS", "INFO:AL7"])
    fun `canHandleCommand should return true when called with valid command`(command: String) {
        val actualResult = commandHandler.canHandleCommand(command)
        assertThat(actualResult).isTrue
    }

    @ParameterizedTest
    @ValueSource(strings = ["INFO", "INFO:", "INFO:AL", "INFO:AL10", "CMD:REBOOT", "CMD:RSP"])
    fun `canHandleCommand should return false when called with invalid command`(command: String) {
        val actualResult = commandHandler.canHandleCommand(command)
        assertThat(actualResult).isFalse
    }

    @Test
    fun `handleCommand should handle info alarms command`() {
        val command = "INFO:ALARMS"
        val alarmThresholdValues = AlarmThresholdValues(6, 0, 500, 1000, 1500, 10)
        simulatorState.addAlarmThresholds(alarmThresholdValues)

        commandHandler.handleCommand(command, simulatorState)

        val expectedDownlink =
            """
            "INFO:ALARMS",{"AL0":[0,0,0,0,0],"AL1":[0,0,0,0,0],"AL2":[0,0,0,0,0],"AL3":[0,0,0,0,0],
            "AL4":[0,0,0,0,0],"AL5":[0,0,0,0,0],"AL6":[0,500,1000,1500,10],"AL7":[0,0,0,0,0]}
        """
                .trimIndent()
                .replace("\n", "")
                .replace("\r", "")
        assertThat(simulatorState.getUrcListForDeviceMessage()).contains(DeviceMessageDownlink(expectedDownlink))
    }

    @Test
    fun `handleCommand should handle info alarm command for a specific alarm`() {
        val command = "INFO:AL7"
        val alarmThresholdValues = AlarmThresholdValues(7, 0, 500, 1000, 1500, 10)
        simulatorState.addAlarmThresholds(alarmThresholdValues)

        commandHandler.handleCommand(command, simulatorState)

        val expectedDownlink = """"INFO:AL7",{"AL7":[0,500,1000,1500,10]}"""
        assertThat(simulatorState.getUrcListForDeviceMessage()).contains(DeviceMessageDownlink(expectedDownlink))
    }

    @ParameterizedTest
    @ValueSource(strings = ["INFO", "INFO:", "INFO:AL", "INFO:AL10", "CMD:REBOOT", "CMD:RSP"])
    fun `handleCommand should throw an exception and not change simulator state when called with invalid command`(
        command: String
    ) {
        assertThatIllegalArgumentException().isThrownBy { commandHandler.handleCommand(command, simulatorState) }

        assertThat(simulatorState.getUrcListForDeviceMessage()).doesNotContain(DeviceMessageDownlink(command))
    }
}
