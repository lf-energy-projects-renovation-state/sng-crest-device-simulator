// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.data.entity

import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink

class SimulatorState(val deviceId: String, var fotaMessageCounter: Int = 0) {

    private val urcs = mutableListOf<String>()
    private val downlinks = mutableListOf<String>()

    fun getUrcListForDeviceMessage(): List<Any> = urcs + listOf(DeviceMessageDownlink(downlinks.joinToString(";")))

    fun resetUrcs() {
        urcs.clear()
        downlinks.clear()
    }

    fun addUrc(urc: String) = apply { urcs += urc }

    fun addDownlink(downlink: String) = apply { downlinks += downlink }
}
