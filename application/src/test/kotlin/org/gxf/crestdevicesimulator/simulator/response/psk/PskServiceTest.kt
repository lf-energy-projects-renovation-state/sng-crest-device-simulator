// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.psk

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import org.apache.commons.codec.digest.DigestUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchException
import org.gxf.crestdevicesimulator.configuration.AdvancedSingleIdentityPskStore
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKey
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKeyStatus
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.gxf.crestdevicesimulator.simulator.event.MessageSentEvent
import org.gxf.crestdevicesimulator.simulator.message.DeviceMessage
import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskEqualityException
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskHashException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PskServiceTest {

    @MockK private lateinit var pskRepository: PskRepository

    @MockK private lateinit var simulatorProperties: SimulatorProperties

    @SpyK private val pskStore = spyk(AdvancedSingleIdentityPskStore(IDENTITY))

    @InjectMockKs private lateinit var pskService: PskService

    private lateinit var oldPsk: PreSharedKey
    private lateinit var newPsk: PreSharedKey

    @BeforeEach
    fun setUp() {
        every { simulatorProperties.pskIdentity } returns IDENTITY

        oldPsk = PreSharedKey(IDENTITY, OLD_REVISION, OLD_KEY, SECRET, PreSharedKeyStatus.ACTIVE)
        every {
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(IDENTITY, PreSharedKeyStatus.ACTIVE)
        } returns oldPsk

        newPsk = PreSharedKey(IDENTITY, NEW_REVISION, NEW_KEY, SECRET, PreSharedKeyStatus.PENDING)
        every {
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(IDENTITY, PreSharedKeyStatus.PENDING)
        } returns newPsk

        pskStore.key = OLD_KEY
    }

    @Test
    fun `should save pending key when psk command is valid`() {
        val expectedHash = DigestUtils.sha256Hex("$SECRET$NEW_KEY")
        val pskCommand = "PSK:$NEW_KEY:${expectedHash}"

        every { pskRepository.save(any<PreSharedKey>()) } returns newPsk

        val actualKey = pskService.preparePendingKey(pskCommand)

        assertThat(pskStore.key).isEqualTo(OLD_KEY)
        assertThat(actualKey).isEqualTo(newPsk)
    }

    @Test
    fun `should throw error when psk command contains invalid hash`() {
        val invalidHash = DigestUtils.sha256Hex("invalid")
        val pskCommand = "PSK:$NEW_KEY:$invalidHash"

        val thrownException = catchException { pskService.preparePendingKey(pskCommand) }

        assertThat(thrownException).isInstanceOf(InvalidPskHashException::class.java)
        assertThat(pskStore.key).isEqualTo(OLD_KEY)
    }

    @Test
    fun `should not throw an error when psk set command is valid`() {
        val expectedHash = DigestUtils.sha256Hex("$SECRET$NEW_KEY")
        val pskCommand = "PSK:$NEW_KEY:${expectedHash}:SET"

        val thrownException = catchException { pskService.verifyPendingKey(pskCommand) }

        assertThat(thrownException).isNull()
        assertThat(pskStore.key).isEqualTo(OLD_KEY)
    }

    @Test
    fun `should throw error when psk set command contains invalid hash`() {
        val invalidHash = DigestUtils.sha256Hex("invalid")
        val pskCommand = "PSK:$NEW_KEY:$invalidHash:SET"

        val thrownException = catchException { pskService.verifyPendingKey(pskCommand) }

        assertThat(thrownException).isInstanceOf(InvalidPskHashException::class.java)
        assertThat(pskStore.key).isEqualTo(OLD_KEY)
    }

    @Test
    fun `should throw error when psk set command contains different key`() {
        val expectedHash = DigestUtils.sha256Hex("$SECRET$NEW_KEY")
        val pskCommand = "PSK:$OLD_KEY:$expectedHash:SET"

        val thrownException = catchException { pskService.verifyPendingKey(pskCommand) }

        assertThat(thrownException).isInstanceOf(InvalidPskEqualityException::class.java)
        assertThat(pskStore.key).isEqualTo(OLD_KEY)
    }

    @Test
    fun `should activate psk and update psk store with new key when message with psk set urc has been sent`() {
        val message = DeviceMessage()
        message.urc = listOf("PSK:SET", DeviceMessageDownlink("PSK:################:SET"))
        val messageSentEvent = MessageSentEvent(message)

        every { pskRepository.save(oldPsk) } returns oldPsk
        every { pskRepository.save(newPsk) } returns newPsk

        pskService.handleMessageSent(messageSentEvent)

        assertThat(oldPsk.status).isEqualTo(PreSharedKeyStatus.INACTIVE)
        assertThat(newPsk.status).isEqualTo(PreSharedKeyStatus.ACTIVE)
        assertThat(pskStore.key).isEqualTo(NEW_KEY)
    }

    companion object {
        private const val IDENTITY = "867787050253370"
        private const val NEW_KEY = "7654321987654321"
        private const val NEW_REVISION = 1
        private const val OLD_KEY = "1234567891234567"
        private const val OLD_REVISION = 0
        private const val SECRET = "secret"
    }
}
