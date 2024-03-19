// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.command

import org.apache.commons.codec.digest.DigestUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchException
import org.gxf.crestdevicesimulator.configuration.AdvancedSingleIdentityPskStore
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKey
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKeyStatus
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.gxf.crestdevicesimulator.simulator.response.command.exception.InvalidPskHashException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class PskCommandHandlerTest {

    @Mock
    private lateinit var pskRepository: PskRepository

    @Mock
    private lateinit var simulatorProperties: SimulatorProperties

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private lateinit var pskStore: AdvancedSingleIdentityPskStore

    @InjectMocks
    private lateinit var pskCommandHandler: PskCommandHandler

    private val newKey = "7654321987654321"

    private val oldKey = "1234567891234567"

    private val secret = "secret"

    private val identity = "1234"

    private val oldRevision = 0

    private val newRevision = 1

    @BeforeEach
    fun setup() {
        val psk = PreSharedKey(identity, oldRevision, oldKey, secret, PreSharedKeyStatus.ACTIVE)
        whenever(simulatorProperties.pskIdentity).thenReturn(identity)
        whenever(pskRepository.findLatestPskForIdentityWithStatus(any<String>(), any())).thenReturn(
            psk
        )
        pskStore.key = oldKey
    }

    @Test
    fun shouldSetNewPskInStoreWhenTheKeyIsValid() {
        val expectedHash = DigestUtils.sha256Hex("$secret$newKey")
        val pskCommand = "!PSK:$newKey$expectedHash;PSK:$newKey${expectedHash}SET"
        val savedPsk = PreSharedKey(
            identity,
            newRevision,
            newKey,
            secret,
            PreSharedKeyStatus.PENDING
        )
        whenever(pskRepository.save(any<PreSharedKey>())).thenReturn(savedPsk)

        assertThat(pskCommandHandler.handlePskChange(pskCommand)).isEqualTo(savedPsk)
//        assertThat(pskStore.key).isEqualTo(newKey) todo add new test for this
    }

    @Test
    fun shouldThrowErrorWhenHashDoesNotMatch() {
        val invalidHash = DigestUtils.sha256Hex("invalid")
        val pskCommand = "!PSK:$oldKey$invalidHash;PSK:$oldKey${invalidHash}SET"

        val thrownException = catchException {
            pskCommandHandler.handlePskChange(pskCommand)
        }
        
        assertThat(thrownException).isInstanceOf(InvalidPskHashException::class.java)
        verify(pskRepository, never()).save(any())
        assertThat(pskStore.key).isEqualTo(oldKey)
    }
}
