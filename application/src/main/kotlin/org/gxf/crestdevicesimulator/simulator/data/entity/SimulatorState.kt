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
        mutableMapOf(
            0 to defaultAlarmThresholdValues(0),
            1 to defaultAlarmThresholdValues(1),
            2 to defaultAlarmThresholdValues(2),
            3 to defaultAlarmThresholdValues(3),
            4 to defaultAlarmThresholdValues(4),
            5 to defaultAlarmThresholdValues(5),
            6 to defaultAlarmThresholdValues(6),
            7 to defaultAlarmThresholdValues(7),
        )

    private fun defaultAlarmThresholdValues(channel: Int) = AlarmThresholdValues(channel, 0, 0, 0, 0, 0)

    fun getUrcListForDeviceMessage(): List<Any> = urcs + listOf(DeviceMessageDownlink(downlinks.joinToString(",")))

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
