// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.response

import org.springframework.stereotype.Component

@Component
class PskKeyExtractor {

    private val pskCommandVerificationRegex = "!PSK:[a-zA-Z0-9]{16}[a-zA-Z0-9]{64};PSK:[a-zA-Z0-9]{16}[a-zA-Z0-9]{64}SET".toRegex()

    /**
     * Regex to split a valid PSK set command in 3 groups
     * Group 0 containing everything after PSK: this includes the key and hash
     * Group 1 containing the next 16 chars after PSK: this is only the key
     * Group 2 containing the next 64 chars after the key this is only the hash
     */
    private val pskKeyHashSplitterRegex = "(?<=PSK:)([a-zA-Z0-9]{16})([a-zA-Z0-9]{64})".toRegex()

    fun hasPskCommand(command: String) = pskCommandVerificationRegex.matches(command)

    fun extractKeyFromCommand(command: String) = pskKeyHashSplitterRegex.findAll(command).first().groups[1]!!.value

    fun extractHashFromCommand(command: String) = pskKeyHashSplitterRegex.findAll(command).first().groups[2]!!.value
}
