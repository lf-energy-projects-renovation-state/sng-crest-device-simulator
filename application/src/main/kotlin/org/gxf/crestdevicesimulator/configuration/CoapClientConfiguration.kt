// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.configuration

import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKey
import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKeyStatus
import org.gxf.crestdevicesimulator.simulator.data.repository.PskRepository
import org.springframework.context.annotation.Bean

@org.springframework.context.annotation.Configuration
class CoapClientConfiguration(private val simulatorProperties: SimulatorProperties,
                              private val pskRepository: PskRepository) {

    @Bean
    fun pskStore(): AdvancedSingleIdentityPskStore {
        val store = AdvancedSingleIdentityPskStore(simulatorProperties.pskIdentity)
        val savedKey = pskRepository.findLatestPskForIdentityWithStatus(
            simulatorProperties.pskIdentity,
            PreSharedKeyStatus.ACTIVE
        )

        if (savedKey == null) {
            val initialPreSharedKey = PreSharedKey(
                simulatorProperties.pskIdentity,
                0,
                simulatorProperties.pskKey,
                simulatorProperties.pskSecret,
                PreSharedKeyStatus.ACTIVE
            )
            pskRepository.save(initialPreSharedKey)
            store.key = simulatorProperties.pskKey
        } else {
            store.key = savedKey.preSharedKey
        }

        return store
    }
}
