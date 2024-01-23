// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.response

import org.springframework.stereotype.Component

@Component
class PskKeyExtractor {

    private val pskCommandVerificationRegex = "PSK:[a-zA-Z0-9]{16}[a-zA-Z0-9]{64};PSK:[a-zA-Z0-9]{16}[a-zA-Z0-9]{64}SET!".toRegex()
    private val pskKeyHashSplitterRegex = "(?<=PSK:)([a-zA-Z0-9]{16})[a-zA-Z0-9]{64}".toRegex()

    fun hasPskCommand(command: String) = pskCommandVerificationRegex.matches(command)

    fun extractKeyFromCommand(command: String) = pskKeyHashSplitterRegex.findAll(command).first().value

    fun extractHashFromCommand(command: String) = pskKeyHashSplitterRegex.findAll(command).last().value
}
