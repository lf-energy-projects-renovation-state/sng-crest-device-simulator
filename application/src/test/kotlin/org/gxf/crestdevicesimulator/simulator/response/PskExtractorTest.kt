// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class PskExtractorTest {

    companion object {
        private const val testHash = "1234567890123456123456789012345612345678901234561234567890123456"

        private const val validPskCommand = "!PSK:1234567891234567${testHash};PSK:1234567891234567${testHash}SET"
        private const val validPskCommandWithKeyWordsInKey = "!PSK:PSKaSET1PSKd2SET${testHash};PSK:PSKaSET1PSKd2SET${testHash}SET"
        private const val invalidKeySizePskCommand = "!PSK:1234${testHash};PSK:1234${testHash}SET"
        private const val notPskCommand = "NoPskCommandInThisString"
    }


    @ParameterizedTest
    @CsvSource(
            "$validPskCommand, true",
            "$validPskCommandWithKeyWordsInKey, true",
            "$invalidKeySizePskCommand, false",
            "$notPskCommand, false"
    )
    fun shouldReturnTrueWhenThereIsAPskCommandInString(pskCommand: String, isValid: Boolean) {
        val result = PskExtractor.hasPskSetCommand(pskCommand)
        assertThat(result).isEqualTo(isValid)
    }

    @ParameterizedTest
    @CsvSource(
            "$validPskCommand, 1234567891234567",
            "$validPskCommandWithKeyWordsInKey, PSKaSET1PSKd2SET"
    )
    fun shouldReturnPskKeyFromValidPskCommand(pskCommand: String, expectedKey: String) {
        val result = PskExtractor.extractKeyFromCommand(pskCommand)

        assertThat(result).isEqualTo(expectedKey)
    }

    @ParameterizedTest
    @CsvSource(
            "$validPskCommand, $testHash",
            "$validPskCommandWithKeyWordsInKey, $testHash"
    )
    fun shouldReturnHashFromValidPskCommand(pskCommand: String, expectedHash: String) {
        val result = PskExtractor.extractHashFromCommand(pskCommand)

        assertThat(result).isEqualTo(expectedHash)
    }
}
