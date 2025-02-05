// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.psk

import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskException

object PskExtractor {

    /**
     * Regex to split a valid PSK command in 3 (or 4 in case of PSK SET command) groups:
     * - Group 0 containing everything;
     * - Group 1 containing the next 16 chars after PSK: this is only the key;
     * - Group 2 containing the next 64 chars after the key this is only the hash.
     * - Group 3 containing :SET when present
     */
    private val pskKeyHashSplitterRegex = "^PSK:([a-zA-Z0-9]{16}):([a-zA-Z0-9]{64})(:SET)?$".toRegex()

    fun extractKeyFromCommand(command: String) = extractGroups(command)[1]!!.value

    fun extractHashFromCommand(command: String) = extractGroups(command)[2]!!.value

    private fun extractGroups(command: String): MatchGroupCollection {
        val matchingGroups = pskKeyHashSplitterRegex.findAll(command)
        if (matchingGroups.none()) throw InvalidPskException("Command did not match psk (set) command")
        return matchingGroups.first().groups
    }
}
