// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import org.assertj.core.api.Assertions.assertThat
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
        simulatorState = SimulatorState()
        simulatorState.resetUrc()
    }

    @Test
    fun `should handle info alarm command`() {
        val command = "INFO:ALARM"
        val alarmThresholdValues = AlarmThresholdValues(6, 0, 500, 1000, 1500, 10)
        simulatorState.addAlarmThresholds(alarmThresholdValues)

        commandHandler.handleCommand(command, simulatorState)

        val expectedDownlink =
            "\"INFO:ALARM\"," +
                "{\"AL0\":[0,0,0,0,0],\"AL1\":[0,0,0,0,0],\"AL2\":[0,0,0,0,0],\"AL3\":[0,0,0,0,0]," +
                "\"AL4\":[0,0,0,0,0],\"AL5\":[0,0,0,0,0],\"AL6\":[0,500,1000,1500,10],\"AL7\":[0,0,0,0,0]}"
        assertThat(simulatorState.getUrcListForDeviceMessage()).contains(DeviceMessageDownlink(expectedDownlink))
    }

    @Test
    fun `should handle info alarm command for a specific alarm`() {
        val command = "INFO:AL7"
        val alarmThresholdValues = AlarmThresholdValues(7, 0, 500, 1000, 1500, 10)
        simulatorState.addAlarmThresholds(alarmThresholdValues)

        commandHandler.handleCommand(command, simulatorState)

        val expectedDownlink = "\"INFO:AL7\",{\"AL7\":[0,500,1000,1500,10]}"
        assertThat(simulatorState.getUrcListForDeviceMessage()).contains(DeviceMessageDownlink(expectedDownlink))
    }

    @ParameterizedTest
    @ValueSource(strings = ["CMD:REBOOT", "CMD:RSP"])
    fun `should not handle other commands`(command: String) {
        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage()).doesNotContain(DeviceMessageDownlink(command))
    }
}
