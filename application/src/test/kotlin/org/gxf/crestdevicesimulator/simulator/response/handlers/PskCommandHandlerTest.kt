// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.apache.commons.codec.digest.DigestUtils
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKey
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKeyStatus
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink
import org.gxf.crestdevicesimulator.simulator.response.psk.PskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class PskCommandHandlerTest {
    @MockK lateinit var pskService: PskService

    @InjectMockKs lateinit var commandHandler: PskCommandHandler

    private lateinit var simulatorState: SimulatorState

    @BeforeEach
    fun setUp() {
        simulatorState = SimulatorState()
        simulatorState.resetUrc()
    }

    @Test
    fun `should handle PSK command`() {
        val hash = DigestUtils.sha256Hex("$SECRET$KEY")
        val command = "PSK:$KEY:$hash"

        val psk = PreSharedKey("", 1, KEY, SECRET, PreSharedKeyStatus.PENDING)
        every { pskService.preparePendingKey(command) } returns psk

        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .contains(URC_SUCCESS)
            .contains(DeviceMessageDownlink(DOWNLINK))
    }

    @ParameterizedTest
    @ValueSource(
        strings =
            [
                "CMD:REBOOT",
                "PSK:1234",
                "PSK:0123456789ABCDEF:0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF:SET",
            ]
    )
    fun `should not handle other commands`(command: String) {
        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .doesNotContain(URC_SUCCESS)
            .doesNotContain(DeviceMessageDownlink(command))
    }

    companion object {
        private const val DOWNLINK = "PSK:################"
        private const val KEY = "0123456789ABCDEF"
        private const val URC_SUCCESS = "PSK:TMP"
        private const val SECRET = "secret"
    }
}
