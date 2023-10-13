// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.generiekemeldersimulator.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "simulator.config")
class SimulatorProperties(
        val localTesting: Boolean,
        val useDtls: Boolean,
        val port: Int,
        val dtlsPort: Int,
        val localHost: String,
        val remoteHost: String,
        val path: String,
        val pskIdentity: String,
        val pskKey: String,
        val messagePath: String,
        val produceValidCbor: Boolean,
)
