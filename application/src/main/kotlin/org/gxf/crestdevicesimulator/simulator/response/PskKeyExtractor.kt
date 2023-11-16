package org.gxf.crestdevicesimulator.simulator.response

import org.springframework.stereotype.Component

@Component
class PskKeyExtractor {

    fun hasPskCommand(command: String): Boolean {
        return command.contains("(?s)(?<=PSK:).{16}SET".toRegex())
    }

    fun extractKeyFromCommand(command: String): String {
        return "(?s)(?<=PSK:).{16}".toRegex().findAll(command).first().value
    }
}
