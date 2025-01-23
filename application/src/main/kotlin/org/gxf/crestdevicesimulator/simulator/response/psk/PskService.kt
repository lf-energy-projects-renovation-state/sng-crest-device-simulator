// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response.psk

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.apache.commons.codec.digest.DigestUtils
import org.gxf.crestdevicesimulator.configuration.AdvancedSingleIdentityPskStore
import org.gxf.crestdevicesimulator.configuration.SimulatorProperties
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKey
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKeyStatus
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.gxf.crestdevicesimulator.simulator.event.MessageSentEvent
import org.gxf.crestdevicesimulator.simulator.message.DeviceMessage
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskEqualityException
import org.gxf.crestdevicesimulator.simulator.response.exception.InvalidPskHashException
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Transactional
@Service
class PskService(
    private val pskRepository: PskRepository,
    private val simulatorProperties: SimulatorProperties,
    private val pskStore: AdvancedSingleIdentityPskStore,
) {

    private val logger = KotlinLogging.logger {}

    @EventListener
    fun handleMessageSent(messageSentEvent: MessageSentEvent) {
        logger.debug { "Handling message sent event" }
        if (hasPskSetSuccessUrc(messageSentEvent.message) && isPendingKeyPresent()) {
            changeActiveKey()
        }
    }

    fun hasPskSetSuccessUrc(message: DeviceMessage) = message.urc.contains("PSK:SET")

    fun preparePendingKey(body: String): PreSharedKey {
        val key = PskExtractor.extractKeyFromCommand(body)
        val hash = PskExtractor.extractHashFromCommand(body)

        val activePreSharedKey =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                simulatorProperties.pskIdentity,
                PreSharedKeyStatus.ACTIVE,
            ) ?: throw NoSuchElementException("No active psk for identity: ${simulatorProperties.pskIdentity}")

        validateHash(hash, key, activePreSharedKey.secret)

        return saveNewPendingPsk(activePreSharedKey, key)
    }

    fun verifyPendingKey(body: String) {
        val key = PskExtractor.extractKeyFromCommand(body)
        val hash = PskExtractor.extractHashFromCommand(body)

        val pendingPsk =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
                simulatorProperties.pskIdentity,
                PreSharedKeyStatus.PENDING,
            ) ?: throw NoSuchElementException("No pending PSK for identity: ${simulatorProperties.pskIdentity}")

        validateKeys(key, pendingPsk.preSharedKey)
        validateHash(hash, key, pendingPsk.secret)
    }

    private fun validateKeys(key: String, pendingKey: String) {
        logger.info { "Validating key for identity: ${simulatorProperties.pskIdentity}" }

        if (key != pendingKey) {
            throw InvalidPskEqualityException(
                "Key in PSK:SET does not match key in PSK command for identity ${simulatorProperties.pskIdentity}"
            )
        }
    }

    private fun validateHash(hash: String, key: String, secret: String) {
        logger.info { "Validating hash for identity: ${simulatorProperties.pskIdentity}" }

        val expectedHash = DigestUtils.sha256Hex("$secret$key")

        if (expectedHash != hash) {
            throw InvalidPskHashException("PSK hash for identity ${simulatorProperties.pskIdentity} did not match")
        }
    }

    private fun saveNewPendingPsk(previousPSK: PreSharedKey, newKey: String): PreSharedKey {
        val newVersion = previousPSK.revision + 1
        logger.debug {
            "Save new key for identity ${simulatorProperties.pskIdentity} with revision $newVersion and status PENDING"
        }
        return pskRepository.save(
            PreSharedKey(previousPSK.identity, newVersion, newKey, previousPSK.secret, PreSharedKeyStatus.PENDING)
        )
    }

    fun isPendingKeyPresent() =
        pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(
            simulatorProperties.pskIdentity,
            PreSharedKeyStatus.PENDING,
        ) != null

    fun changeActiveKey() {
        val identity = simulatorProperties.pskIdentity
        val currentPsk =
            pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(identity, PreSharedKeyStatus.ACTIVE)
        val newPsk = pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(identity, PreSharedKeyStatus.PENDING)

        check(currentPsk != null && newPsk != null) { "No current or new psk, impossible to change active key" }

        currentPsk.status = PreSharedKeyStatus.INACTIVE
        newPsk.status = PreSharedKeyStatus.ACTIVE
        pskRepository.save(currentPsk)
        pskRepository.save(newPsk)
        pskStore.key = newPsk.preSharedKey
    }

    fun setPendingKeyAsInvalid() {
        val identity = simulatorProperties.pskIdentity
        val psk = pskRepository.findFirstByIdentityAndStatusOrderByRevisionDesc(identity, PreSharedKeyStatus.PENDING)

        if (psk != null) {
            psk.status = PreSharedKeyStatus.INVALID
            pskRepository.save(psk)
        }
    }
}
