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
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.gxf.crestdevicesimulator.simulator.response.command.exception.InvalidPskHashException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

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

    @BeforeEach
    fun setup() {
        `when`(simulatorProperties.pskIdentity).thenReturn(identity)
        `when`(pskRepository.findById(any())).thenReturn(Optional.of(PreSharedKey(identity, oldKey, secret)))
        pskStore.key = oldKey
    }

    @Test
    fun shouldSetNewPskInStoreWhenTheKeyIsValid() {
        val expectedHash = DigestUtils.sha256Hex("$secret$newKey")
        val pskCommand = "!PSK:$newKey:$expectedHash;PSK:$newKey:${expectedHash}SET"

        pskCommandHandler.handlePskChange(pskCommand)

        assertThat(pskStore.key).isEqualTo(newKey)
    }

    @Test
    fun shouldThrowErrorWhenHashDoesNotMatch() {
        val invalidHash = DigestUtils.sha256Hex("invalid")
        val pskCommand = "!PSK:$oldKey:$invalidHash;PSK:$oldKey:${invalidHash}SET"

        val thrownException = catchException {
            pskCommandHandler.handlePskChange(pskCommand)
        }
        
        assertThat(thrownException).isInstanceOf(InvalidPskHashException::class.java)
        verify(pskRepository, never()).save(any())
        assertThat(pskStore.key).isEqualTo(oldKey)
    }
}
