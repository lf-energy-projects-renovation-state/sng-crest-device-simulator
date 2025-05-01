// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.data.entity

import java.time.Instant
import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink

/**
 * Simulator state
 *
 * @property fotaMessageCounter the counter used for firmware updates, defaults to 0
 * @property urcs the URCs that will be sent in the next message, defaults to INIT at startup
 */
class SimulatorState(var fotaMessageCounter: Int = 0, private val urcs: MutableList<Any> = mutableListOf("INIT")) {
    private val downlinks: MutableList<String> = mutableListOf()
    val alarmThresholds: MutableMap<Int, AlarmThresholdValues> = defaultAlarmThresholds()

    var mem: Int = 0
        private set

    var tsl: Int = 0
        private set

    private fun defaultAlarmThresholds() =
        (0..7).associateWith { AlarmThresholdValues(it, 0, 0, 0, 0, 0) }.toMutableMap()

    fun getUrcListForDeviceMessage(): List<Any> = urcs + DeviceMessageDownlink(downlinks.joinToString(","))

    fun resetUrc() {
        urcs.clear()
        downlinks.clear()
    }

    fun addUrc(urc: Any) = apply { urcs += urc }

    fun addDownlink(downlink: String) = apply { downlinks += downlink }

    fun addAlarmThresholds(alarmThresholdValues: AlarmThresholdValues) = apply {
        alarmThresholds[alarmThresholdValues.channel] = alarmThresholdValues
    }

    fun getAlarmThresholds(index: Int) = alarmThresholds[index]

    fun requestSucceeded() {
        mem = 0 // pretend all flash messages have been sent
        tsl = Instant.now().epochSecond.toInt()
    }

    fun requestFailed() {
        mem++
    }
}
