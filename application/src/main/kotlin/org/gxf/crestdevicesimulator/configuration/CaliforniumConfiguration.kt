// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.configuration

import org.eclipse.californium.core.config.CoapConfig
import org.eclipse.californium.elements.config.Configuration
import org.eclipse.californium.elements.config.TcpConfig
import org.eclipse.californium.elements.config.UdpConfig
import org.eclipse.californium.scandium.config.DtlsConfig
import org.eclipse.californium.scandium.config.DtlsConfig.DtlsRole
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite
import org.springframework.context.annotation.Bean

@org.springframework.context.annotation.Configuration
class CaliforniumConfiguration(private val simulatorProperties: SimulatorProperties) {

    init {
        DtlsConfig.register()
        CoapConfig.register()
        UdpConfig.register()
        TcpConfig.register()
    }

    @Bean
    fun configure(): Configuration {
        return Configuration.getStandard()
                .set(CoapConfig.COAP_PORT, simulatorProperties.uri.port)
                .set(CoapConfig.COAP_SECURE_PORT, simulatorProperties.uri.port)
                .set(DtlsConfig.DTLS_ROLE, DtlsRole.CLIENT_ONLY)
                .set(DtlsConfig.DTLS_RECOMMENDED_CIPHER_SUITES_ONLY, false)
                .set(DtlsConfig.DTLS_CIPHER_SUITES, listOf(CipherSuite.getTypeByName(simulatorProperties.cipherSuite)))
    }
}
