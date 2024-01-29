package org.gxf.crestdevicesimulator.simulator.response

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PskExtractorTest {

    private val testHash = "1234567890123456123456789012345612345678901234561234567890123456"

    private val validPskCommand = "!PSK:1234567891234567${testHash};PSK:1234567891234567${testHash}SET"
    private val validPskCommandWithKeyWordsInKey = "!PSK:PSKaSET1PSKd2SET${testHash};PSK:PSKaSET1PSKd2SET${testHash}SET"
    private val invalidKeySizePskCommand = "!PSK:1234${testHash};PSK:1234${testHash}SET"
    private val notPskCommand = "NoPskCommandInThisString"


    @Test
    fun shouldReturnTrueWhenThereIsAPskCommandInString() {
        val resultValid = PskExtractor.hasPskCommand(validPskCommand)
        val resultValidWithKeyWords = PskExtractor.hasPskCommand(validPskCommandWithKeyWordsInKey)
        val resultInvalidKeySize = PskExtractor.hasPskCommand(invalidKeySizePskCommand)
        val resultNoPskCommand = PskExtractor.hasPskCommand(notPskCommand)

        assertTrue(resultValid)
        assertTrue(resultValidWithKeyWords)
        assertFalse(resultInvalidKeySize)
        assertFalse(resultNoPskCommand)
    }

    @Test
    fun shouldReturnPskKeyFromValidPskCommand() {
        val resultValid = PskExtractor.extractKeyFromCommand(validPskCommand)
        val resultValidWithKeyWords = PskExtractor.extractKeyFromCommand(validPskCommandWithKeyWordsInKey)

        assertEquals("1234567891234567", resultValid)
        assertEquals("PSKaSET1PSKd2SET", resultValidWithKeyWords)
    }
}
