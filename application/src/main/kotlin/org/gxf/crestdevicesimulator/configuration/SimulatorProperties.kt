// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.configuration

import java.net.URI
import java.time.Duration
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "simulator.config")
class SimulatorProperties(
    val uri: URI,
    val pskIdentity: String,
    val pskKey: String,
    val pskSecret: String,
    val sleepDuration: Duration,
    val produceValidCbor: Boolean,
    val cipherSuites: List<CipherSuite>,
)
