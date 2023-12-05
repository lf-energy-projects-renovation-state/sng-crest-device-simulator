// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.response

import org.springframework.stereotype.Component

@Component
class PskKeyExtractor {

    private val pskCommandVerificationRegex = "PSK:[a-zA-Z0-9]{16};PSK:[a-zA-Z0-9]{16}SET".toRegex()
    private val pskExtractorRegex = "(?<=PSK:)[a-zA-Z0-9]{16}".toRegex()

    fun hasPskCommand(command: String) = pskCommandVerificationRegex.matches(command)

    fun extractKeyFromCommand(command: String) = pskExtractorRegex.findAll(command).first().value
}
