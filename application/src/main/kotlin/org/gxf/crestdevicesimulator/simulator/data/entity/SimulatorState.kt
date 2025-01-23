// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.data.entity

import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink

class SimulatorState(var fotaMessageCounter: Int = 0) {

    private val urcs = mutableListOf("INIT") // INIT = boot, will be reset for second message
    private val downlinks = mutableListOf<String>()

    private val alarmThresholds = mutableMapOf<Int, AlarmThresholdValues>()

    fun getUrcListForDeviceMessage(): List<Any> = urcs + listOf(DeviceMessageDownlink(downlinks.joinToString(";")))

    fun resetUrc() {
        urcs.clear()
        downlinks.clear()
    }

    fun addUrc(urc: String) = apply { urcs += urc }

    fun addDownlink(downlink: String) = apply { downlinks += downlink }

    fun addAlarmThresholds(alarmThresholdValues: AlarmThresholdValues) = apply {
        alarmThresholds[alarmThresholdValues.index] = alarmThresholdValues
    }

    fun getAlarmThresholds(index: Int) = alarmThresholds[index]
}
