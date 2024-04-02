// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.response.command

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdevicesimulator.configuration.AdvancedSingleIdentityPskStore
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKey
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKeyStatus
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.gxf.crestdevicesimulator.simulator.response.PskExtractor
import org.gxf.crestdevicesimulator.simulator.response.command.exception.InvalidPskHashException
import org.springframework.stereotype.Service

@Transactional
@Service
class PskService(
    private val pskRepository: PskRepository,
    private val simulatorProperties: SimulatorProperties,
    private val pskStore: AdvancedSingleIdentityPskStore
) {

    private val logger = KotlinLogging.logger {}

    fun preparePendingKey(body: String): PreSharedKey {
        val newPsk = PskExtractor.extractKeyFromCommand(body)
        val hash = PskExtractor.extractHashFromCommand(body)

        val activePreSharedKey = pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
            simulatorProperties.pskIdentity,
            PreSharedKeyStatus.ACTIVE
        )
            ?: throw NoSuchElementException("No active psk for identity: ${simulatorProperties.pskIdentity}")

        logger.info { "Validating hash for identity: ${simulatorProperties.pskIdentity}" }

        val secret = activePreSharedKey.secret
        val expectedHash = DigestUtils.sha256Hex("$secret$newPsk")

        if (expectedHash != hash) {
            throw InvalidPskHashException("PSK set Hash for Identity ${simulatorProperties.pskIdentity} did not match")
        }

        return setNewKeyForIdentity(activePreSharedKey, newPsk)
    }

    private fun setNewKeyForIdentity(previousPSK: PreSharedKey, newKey: String): PreSharedKey {
        val newVersion = previousPSK.revision + 1
        logger.debug { "Save new key for identity ${simulatorProperties.pskIdentity} with revision $newVersion and status PENDING" }
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

    fun isPendingKeyPresent() = pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
        simulatorProperties.pskIdentity,
        PreSharedKeyStatus.PENDING
    ) != null

    fun changeActiveKey() {
        val identity = simulatorProperties.pskIdentity
        val currentPsk =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                identity,
                PreSharedKeyStatus.ACTIVE
            )
        val newPsk =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                identity,
                PreSharedKeyStatus.PENDING
            )

        check(currentPsk != null && newPsk != null) { "No current or new psk, impossible to change active key" }

        currentPsk.status = PreSharedKeyStatus.INACTIVE
        newPsk.status = PreSharedKeyStatus.ACTIVE
        pskRepository.save(currentPsk)
        pskRepository.save(newPsk)
        pskStore.key = newPsk.preSharedKey
    }

    fun setPendingKeyAsInvalid() {
        val identity = simulatorProperties.pskIdentity
        val psk =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                identity,
                PreSharedKeyStatus.PENDING
            )

        if (psk != null) {
            psk.status = PreSharedKeyStatus.INVALID
            pskRepository.save(psk)
        }
    }
}
