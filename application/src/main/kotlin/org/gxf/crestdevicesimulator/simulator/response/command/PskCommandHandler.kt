// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.response.command

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdevicesimulator.configuration.AdvancedSingleIdentityPskStore
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKey
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKeyStatus
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.gxf.crestdevicesimulator.simulator.response.PskExtractor
import org.gxf.crestdevicesimulator.simulator.response.command.exception.InvalidPskHashException
import org.springframework.stereotype.Service

@Service
class PskCommandHandler(private val pskRepository: PskRepository,
                        private val simulatorProperties: SimulatorProperties,
                        private val pskStore: AdvancedSingleIdentityPskStore) {

    private val logger = KotlinLogging.logger {}

    fun handlePskChange(body: String) {
        val newPsk = PskExtractor.extractKeyFromCommand(body)
        val hash = PskExtractor.extractHashFromCommand(body)

        val preSharedKey = pskRepository.findLatestActivePsk(simulatorProperties.pskIdentity)

        if (preSharedKey == null) {
            logger.error { "No psk for identity: ${simulatorProperties.pskIdentity}" }
            throw NoSuchElementException()
        }

        logger.info { "Validating hash for identity: ${simulatorProperties.pskIdentity}" }

        val secret = preSharedKey.secret
        val expectedHash = DigestUtils.sha256Hex("$secret$newPsk")

        if (expectedHash != hash) {
            throw InvalidPskHashException("PSK set Hash for Identity ${simulatorProperties.pskIdentity} did not match")
        }

        pskRepository.save(preSharedKey.apply { this.preSharedKey = newPsk })
        pskStore.key = newPsk
    }

    fun setNewKeyForIdentity(identity: String, newKey: String): PreSharedKey {
        val previousPSK = pskRepository.findLatestActivePsk(identity)!!
        val newVersion = previousPSK.revision + 1
        return pskRepository.save(
            PreSharedKey(
                identity,
                newVersion,
                newKey,
                previousPSK.secret,
                PreSharedKeyStatus.PENDING
            )
        )
    }
}
