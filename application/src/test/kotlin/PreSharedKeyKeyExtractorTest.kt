import org.gxf.crestdevicesimulator.simulator.response.PskKeyExtractor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PreSharedKeyKeyExtractorTest {

    private val pskKeyExtractor = PskKeyExtractor()

    private val validPskCommand = "PSK:1234567891234567;PSK:1234567891234567SET"
    private val validPskCommandWithKeyWordsInKey = "PSK:PSKaSET1PSKd2SET;PSK:PSKaSET1PSKd2SETSET"
    private val invalidKeySizePskCommand = "PSK:1234;PSK:1234SET"
    private val notPskCommand = "NoPskCommandInThisString"


    @Test
    fun shouldReturnTrueWhenThereIsAPskCommandInString() {
        val resultValid = pskKeyExtractor.hasPskCommand(validPskCommand)
        val resultValidWithKeyWords = pskKeyExtractor.hasPskCommand(validPskCommandWithKeyWordsInKey)
        val resultInvalidKeySize = pskKeyExtractor.hasPskCommand(invalidKeySizePskCommand)
        val resultNoPskCommand = pskKeyExtractor.hasPskCommand(notPskCommand)

        assertTrue(resultValid)
        assertTrue(resultValidWithKeyWords)
        assertFalse(resultInvalidKeySize)
        assertFalse(resultNoPskCommand)
    }

    @Test
    fun shouldReturnPskKeyFromValidPskCommand() {
        val resultValid = pskKeyExtractor.extractKeyFromCommand(validPskCommand)
        val resultValidWithKeyWords = pskKeyExtractor.extractKeyFromCommand(validPskCommandWithKeyWordsInKey)

        assertEquals("1234567891234567", resultValid)
        assertEquals("PSKaSET1PSKd2SET", resultValidWithKeyWords)
    }
}
