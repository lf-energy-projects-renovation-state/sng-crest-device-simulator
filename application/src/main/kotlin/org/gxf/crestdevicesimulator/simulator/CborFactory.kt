// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper

object CborFactory {
    val invalidCborMessage = "B36249441B000313"

    fun createValidCbor(jsonNode: JsonNode): ByteArray = CBORMapper().writeValueAsBytes(jsonNode)

    fun createInvalidCbor(): ByteArray = invalidCborMessage.toByteArray()
}
