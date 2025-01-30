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

class AlarmCommandHandlerTest {
    private val commandHandler = AlarmCommandHandler()

    private lateinit var simulatorState: SimulatorState

    @BeforeEach
    fun setUp() {
        simulatorState = SimulatorState()
        simulatorState.resetUrc()
        simulatorState.resetAlarmThresholds()
    }

    @ParameterizedTest
    @ValueSource(strings = ["AL6:100,200,300,400,10", "AL6:-400,-300,-200,-100,-10"])
    fun `canHandleCommand should return true when called with valid command`(command: String) {
        val actualResult = commandHandler.canHandleCommand(command)
        assertThat(actualResult).isTrue()
    }

    @ParameterizedTest
    @ValueSource(
        strings =
            ["AL", "AL:", "AL2:100", "AL3:100,200", "AL4:100,200,300", "AL5:100,200,300,400", "CMD:REBOOT", "CMD:RSP"]
    )
    fun `canHandleCommand should return false when called with invalid command`(command: String) {
        val actualResult = commandHandler.canHandleCommand(command)
        assertThat(actualResult).isFalse()
    }

    @Test
    fun `handleCommand should set alarm thresholds and add success urc when called with valid command`() {
        val command = "AL6:0,500,1000,1500,10"
        val expectedAlarmThresholdValues = AlarmThresholdValues(6, 0, 500, 1000, 1500, 10)

        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getAlarmThresholds(6)).isEqualTo(expectedAlarmThresholdValues)
        assertThat(simulatorState.getUrcListForDeviceMessage())
            .contains(URC_SUCCESS)
            .contains(DeviceMessageDownlink(command))
    }

    @ParameterizedTest
    @ValueSource(
        strings =
            [
                "AL6:9999999999,200,300,400,10",
                "AL6:100,9999999999,300,400,10",
                "AL6:100,200,9999999999,400,10",
                "AL6:100,200,300,400,9999999999",
            ]
    )
    fun `handleCommand should not set alarm thresholds and add failure urc when exception occurs`(command: String) {
        val expectedAlarmThresholdValues = AlarmThresholdValues(6, 0, 0, 0, 0, 0)

        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getAlarmThresholds(6)).isEqualTo(expectedAlarmThresholdValues)
        assertThat(simulatorState.getUrcListForDeviceMessage())
            .contains(URC_FAILURE)
            .contains(DeviceMessageDownlink(command))
    }

    @ParameterizedTest
    @ValueSource(
        strings =
            ["AL", "AL:", "AL2:100", "AL3:100,200", "AL4:100,200,300", "AL5:100,200,300,400", "CMD:REBOOT", "CMD:RSP"]
    )
    fun `handleCommand should not do anything when called with invalid command`(command: String) {
        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .doesNotContain(URC_SUCCESS)
            .doesNotContain(DeviceMessageDownlink(command))
    }

    companion object {
        private const val URC_SUCCESS = "AL6:SET"
        private const val URC_FAILURE = "AL6:DLER"
    }
}
