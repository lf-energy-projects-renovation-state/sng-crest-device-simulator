// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.response

import org.springframework.stereotype.Component

@Component
class PskKeyExtractor {

    private val pskCommandRegex = "(?<=PSK:).{16}".toRegex()
    private val pskSetCommandRegex = "(?<=PSK:).{16}SET".toRegex()

    fun hasPskCommand(command: String): Boolean {
        return command.contains(pskCommandRegex) && command.contains(pskSetCommandRegex)
    }

    fun extractKeyFromCommand(command: String): String {
        return pskCommandRegex.findAll(command).first().value
    }
}
