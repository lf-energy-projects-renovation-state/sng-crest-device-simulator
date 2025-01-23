// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.handlers

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import org.apache.commons.codec.digest.DigestUtils
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdevicesimulator.simulator.data.entity.SimulatorState
import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskEqualityException
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskException
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskHashException
import org.gxf.crestdevicesimulator.simulator.response.psk.PskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class PskSetCommandHandlerTest {
    @MockK lateinit var pskService: PskService

    @InjectMockKs lateinit var commandHandler: PskSetCommandHandler

    private lateinit var simulatorState: SimulatorState

    @BeforeEach
    fun setUp() {
        simulatorState = SimulatorState()
        simulatorState.resetUrc()
    }

    @Test
    fun `should handle command when psk set command is valid`() {
        val hash = DigestUtils.sha256Hex("$SECRET$KEY")
        val command = "PSK:$KEY:$hash:SET"

        justRun { pskService.verifyPendingKey(command) }

        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .contains(URC_SUCCESS)
            .contains(DeviceMessageDownlink(DOWNLINK))
    }

    @Test
    fun `should handle command when psk set command contains invalid key`() {
        val invalidKey = "FEDCBA9876543210"
        val hash = DigestUtils.sha256Hex("$SECRET$KEY")
        val command = "PSK:$invalidKey:$hash:SET"

        every { pskService.verifyPendingKey(command) } throws InvalidPskEqualityException("Keys do not match")
        justRun { pskService.setPendingKeyAsInvalid() }

        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .contains(URC_ERROR_KEY)
            .contains(DeviceMessageDownlink(DOWNLINK))
    }

    @Test
    fun `should handle command when psk set command contains invalid hash`() {
        val hash = DigestUtils.sha256Hex("invalid")
        val command = "PSK:$KEY:$hash:SET"

        every { pskService.verifyPendingKey(command) } throws InvalidPskHashException("Invalid hash")
        justRun { pskService.setPendingKeyAsInvalid() }

        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .contains(URC_ERROR_HASH)
            .contains(DeviceMessageDownlink(DOWNLINK))
    }

    @Test
    fun `should handle command when some other error occurs`() {
        val hash = DigestUtils.sha256Hex("$SECRET$KEY")
        val command = "PSK:$KEY:$hash:SET"

        every { pskService.verifyPendingKey(command) } throws InvalidPskException("Some other error")
        justRun { pskService.setPendingKeyAsInvalid() }

        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .contains(URC_ERROR_OTHER)
            .contains(DeviceMessageDownlink(DOWNLINK))
    }

    @ParameterizedTest
    @ValueSource(
        strings =
            ["CMD:REBOOT", "PSK:1234", "PSK:$KEY:0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF"]
    )
    fun `should not handle other commands`(command: String) {
        commandHandler.handleCommand(command, simulatorState)

        assertThat(simulatorState.getUrcListForDeviceMessage())
            .doesNotContain(URC_SUCCESS)
            .doesNotContain(DeviceMessageDownlink(command))
    }

    companion object {
        private const val DOWNLINK = "PSK:################:SET"
        private const val KEY = "0123456789ABCDEF"
        private const val URC_SUCCESS = "PSK:SET"
        private const val URC_ERROR_KEY = "PSK:EQER"
        private const val URC_ERROR_HASH = "PSK:HSER"
        private const val URC_ERROR_OTHER = "PSK:DLER"
        private const val SECRET = "secret"
    }
}
