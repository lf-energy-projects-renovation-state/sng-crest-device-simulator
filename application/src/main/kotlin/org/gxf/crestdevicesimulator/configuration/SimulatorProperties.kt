// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties(prefix = "simulator.config")
class SimulatorProperties(
        val uri: URI,
        val pskIdentity: String,
        val pskKey: String,
        val messagePath: String,
        val produceValidCbor: Boolean,
)
