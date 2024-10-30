// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.message

import com.fasterxml.jackson.databind.PropertyNamingStrategies.NamingBase

class CrestNamingStrategy : NamingBase() {
    override fun translate(propertyName: String?): String? {
        return if (propertyName == "cid") "cID" else propertyName?.uppercase()
    }
}
