package org.gxf.generiekemeldersimulator.simulator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper

object CborFactory {
    val invalidCborMessage = "B36249441B000313"

    fun createValidCbor(jsonNode: JsonNode): ByteArray = CBORMapper().writeValueAsBytes(jsonNode)

    fun createInvalidCbor(): ByteArray = invalidCborMessage.toByteArray()
}