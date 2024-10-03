// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response

import org.gxf.crestdevicesimulator.simulator.response.command.exception.InvalidPskException

object PskExtractor {

    /**
     * Regex to split a valid PSK set command in 3 groups: Group 0 containing everything; Group 1 containing the next 16
     * chars after PSK: this is only the key; Group 2 containing the next 64 chars after the key this is only the hash.
     */
    private val pskKeyHashSplitterRegex =
        "!PSK:([a-zA-Z0-9]{16}):([a-zA-Z0-9]{64});PSK:[a-zA-Z0-9]{16}:[a-zA-Z0-9]{64}:SET".toRegex()

    fun hasPskSetCommand(command: String) = pskKeyHashSplitterRegex.matches(command)

    fun extractKeyFromCommand(command: String) = extractGroups(command)[1]!!.value

    fun extractHashFromCommand(command: String) = extractGroups(command)[2]!!.value

    private fun extractGroups(command: String): MatchGroupCollection {
        val matchingGroups = pskKeyHashSplitterRegex.findAll(command)
        if (matchingGroups.none()) throw InvalidPskException("Command did not match psk set command")

        val groups = matchingGroups.first().groups
        if (groups.size != 3) throw InvalidPskException("Command did not contain psk and hash")
        return groups
    }
}
