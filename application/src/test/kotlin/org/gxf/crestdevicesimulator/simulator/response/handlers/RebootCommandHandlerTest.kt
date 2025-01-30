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

class RebootCommandHandlerTest {
    private val commandHandler = RebootCommandHandler()

    private lateinit var simulatorState: SimulatorState

    @BeforeEach
    fun setUp() {
        simulatorState = SimulatorState()
        simulatorState.resetUrc()
    }

    @ParameterizedTest
    @ValueSource(strings = ["CMD:REBOOT"])
    fun `canHandleCommand should return true when called with valid command`(command: String) {
        val actualResult = commandHandler.canHandleCommand(command)
        assertThat(actualResult).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["CMD", "CMD:", "CMD:REB", "CMD:RSP", "AL6:0,500,1000,1500,10"])
    fun `canHandleCommand should return false when called with invalid command`(command: String) {
        val actualResult = commandHandler.canHandleCommand(command)
        assertThat(actualResult).isFalse()
    }

    @Test
    fun `handleCommand should add init urc when called with valid command`() {
        val command = "CMD:REBOOT"

        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .contains(URC_SUCCESS)
            .contains(DeviceMessageDownlink(command))
    }

    @ParameterizedTest
    @ValueSource(strings = ["CMD", "CMD:", "CMD:REB", "CMD:RSP", "AL6:0,500,1000,1500,10"])
    fun `handleCommand should not do anything when called with invalid command`(command: String) {
        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .doesNotContain(URC_SUCCESS)
            .doesNotContain(DeviceMessageDownlink(command))
    }

    companion object {
        private const val URC_SUCCESS = "INIT"
    }
}
