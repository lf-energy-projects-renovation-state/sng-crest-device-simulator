// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.psk

import org.assertj.core.api.Assertions.*
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskException
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class PskExtractorTest {

    @ParameterizedTest
    @CsvSource(
        "$VALID_PSK_COMMAND, $KEY",
        "$VALID_PSK_COMMAND_WITH_KEYWORDS_IN_KEY, $KEY_WITH_KEYWORDS",
        "$VALID_PSK_SET_COMMAND, $KEY",
        "$VALID_PSK_SET_COMMAND_WITH_KEYWORDS_IN_KEY, $KEY_WITH_KEYWORDS",
    )
    fun `should return key from valid command`(command: String, expectedKey: String) {
        val result = PskExtractor.extractKeyFromCommand(command)

        assertThat(result).isEqualTo(expectedKey)
    }

    @ParameterizedTest
    @CsvSource(
        "$VALID_PSK_COMMAND, $HASH",
        "$VALID_PSK_COMMAND_WITH_KEYWORDS_IN_KEY, $HASH",
        "$VALID_PSK_SET_COMMAND, $HASH",
        "$VALID_PSK_SET_COMMAND_WITH_KEYWORDS_IN_KEY, $HASH",
    )
    fun `should return hash from valid command`(command: String, expectedHash: String) {
        val result = PskExtractor.extractHashFromCommand(command)

        assertThat(result).isEqualTo(expectedHash)
    }

    @ParameterizedTest
    @ValueSource(strings = [INVALID_KEY_SIZE_PSK_COMMAND, INVALID_KEY_SIZE_PSK_SET_COMMAND, NOT_A_PSK_COMMAND])
    fun `should throw exception when command is invalid`(command: String) {
        val throwable = catchThrowable { PskExtractor.extractKeyFromCommand(command) }

        assertThat(throwable).isInstanceOf(InvalidPskException::class.java)
    }

    companion object {
        private const val HASH = "1234567890123456123456789012345612345678901234561234567890123456"
        private const val KEY = "1234567891234567"
        private const val KEY_WITH_KEYWORDS = "PSKaSET1PSKd2SET"
        private const val KEY_TOO_SHORT = "1234"

        private const val VALID_PSK_COMMAND = "PSK:$KEY:$HASH"
        private const val VALID_PSK_COMMAND_WITH_KEYWORDS_IN_KEY = "PSK:$KEY_WITH_KEYWORDS:$HASH"
        private const val VALID_PSK_SET_COMMAND = "PSK:$KEY:$HASH:SET"
        private const val VALID_PSK_SET_COMMAND_WITH_KEYWORDS_IN_KEY = "PSK:$KEY_WITH_KEYWORDS:$HASH:SET"
        private const val INVALID_KEY_SIZE_PSK_COMMAND = "PSK:$KEY_TOO_SHORT:$HASH"
        private const val INVALID_KEY_SIZE_PSK_SET_COMMAND = "PSK:$KEY_TOO_SHORT:$HASH:SET"
        private const val NOT_A_PSK_COMMAND = "NoPskCommandInThisString"
    }
}
