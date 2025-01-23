// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class Rsp2CommandHandlerTest {
    private val commandHandler = Rsp2CommandHandler()

    private lateinit var simulatorState: SimulatorState

    @BeforeEach
    fun setUp() {
        simulatorState = SimulatorState()
        simulatorState.resetUrc()
    }

    @Test
    fun `should handle CMD RSP2 command`() {
        val command = "CMD:RSP2"

        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .contains(URC_SUCCESS)
            .contains(DeviceMessageDownlink(command))
    }

    @ParameterizedTest
    @ValueSource(strings = ["CMD:REBOOT", "CMD:RSP"])
    fun `should not handle other commands`(command: String) {
        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .doesNotContain(URC_SUCCESS)
            .doesNotContain(DeviceMessageDownlink(command))
    }

    companion object {
        private const val URC_SUCCESS = "RSP2:OK"
    }
}
