// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.configuration

import org.eclipse.californium.scandium.dtls.cipher.CipherSuite
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.Resource
import java.net.URI
import java.time.Duration

@ConfigurationProperties(prefix = "simulator.config")
class SimulatorProperties(
        val uri: URI,
        val pskIdentity: String,
        val pskKey: String,
        val pskSecret: String,
        val sleepDuration: Duration,
        val scheduledMessage: Resource,
        val successMessage: Resource,
        val failureMessage: Resource,
        val rebootSuccessMessage: Resource,
        val produceValidCbor: Boolean,
        val cipherSuites: List<CipherSuite>
)
