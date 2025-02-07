// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.data.entity

import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink

class SimulatorState(var fotaMessageCounter: Int = 0) {

    private val urcs = mutableListOf("INIT") // INIT = boot, will be reset for second message
    private val downlinks = mutableListOf<String>()

    private val alarmThresholds = defaultAlarmThresholds()

    private fun defaultAlarmThresholds() =
        (0..7).associateWith { AlarmThresholdValues(it, 0, 0, 0, 0, 0) }.toMutableMap()

    fun getUrcListForDeviceMessage(): List<Any> = urcs + DeviceMessageDownlink(downlinks.joinToString(","))

    fun resetUrc() {
        urcs.clear()
        downlinks.clear()
    }

    fun addUrc(urc: String) = apply { urcs += urc }

    fun addDownlink(downlink: String) = apply { downlinks += downlink }

    fun addAlarmThresholds(alarmThresholdValues: AlarmThresholdValues) = apply {
        alarmThresholds[alarmThresholdValues.channel] = alarmThresholdValues
    }

    fun getAlarmThresholds() = alarmThresholds

    fun getAlarmThresholds(index: Int) = alarmThresholds[index]

    fun resetAlarmThresholds() {
        alarmThresholds.clear()
        alarmThresholds += defaultAlarmThresholds()
    }
}
