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

    fun handlePskChange(body: String): Boolean {
        val newPsk = PskExtractor.extractKeyFromCommand(body)
        val hash = PskExtractor.extractHashFromCommand(body)

        val activePreSharedKey = pskRepository.findLatestPskForIdentityWithStatus(
            simulatorProperties.pskIdentity,
            PreSharedKeyStatus.ACTIVE
        )
            ?: throw NoSuchElementException("No psk for identity: ${simulatorProperties.pskIdentity}")

        logger.info { "Validating hash for identity: ${simulatorProperties.pskIdentity}" }

        val secret = activePreSharedKey.secret
        val expectedHash = DigestUtils.sha256Hex("$secret$newPsk")

        if (expectedHash != hash) {
            throw InvalidPskHashException("PSK set Hash for Identity ${simulatorProperties.pskIdentity} did not match")
        }

        val newPreSharedKey = setNewKeyForIdentity(activePreSharedKey, newPsk)

        pskRepository.save(newPreSharedKey)

        return true
    }

    fun setNewKeyForIdentity(previousPSK: PreSharedKey, newKey: String): PreSharedKey {
        val newVersion = previousPSK.revision + 1
        return pskRepository.save(
            PreSharedKey(
                previousPSK.identity,
                newVersion,
                newKey,
                previousPSK.secret,
                PreSharedKeyStatus.PENDING
            )
        )
    }

    fun changeActiveKey() {
        val identity = simulatorProperties.pskIdentity
        val currentPsk =
            pskRepository.findLatestPskForIdentityWithStatus(identity, PreSharedKeyStatus.ACTIVE)
        val newPsk =
            pskRepository.findLatestPskForIdentityWithStatus(identity, PreSharedKeyStatus.PENDING)

        check(currentPsk != null && newPsk != null) { "No current or new psk, impossible to change active key" }

        // todo zorgen dat dit altijd allemaal of helemaal niet gebeurt
        currentPsk.status = PreSharedKeyStatus.INACTIVE
        newPsk.status = PreSharedKeyStatus.ACTIVE
        pskRepository.save(currentPsk)
        pskRepository.save(newPsk)
        pskStore.key = newPsk.preSharedKey
    }

    fun setPendingKeyAsInvalid() {
        val identity = simulatorProperties.pskIdentity
        val newPsk =
            pskRepository.findLatestPskForIdentityWithStatus(identity, PreSharedKeyStatus.PENDING)

        if (newPsk != null) {
            newPsk.status = PreSharedKeyStatus.INVALID
        }
    }
}
