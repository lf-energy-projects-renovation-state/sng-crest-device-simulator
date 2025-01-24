// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.data.entity

import org.gxf.crestdevicesimulator.simulator.message.DeviceMessageDownlink

class SimulatorState(var fotaMessageCounter: Int = 0) {

    private val urcs = mutableListOf("INIT") // INIT = boot, will be reset for second message
    private val downlinks = mutableListOf<String>()

    private val alarmThresholds =
        mutableMapOf<Int, AlarmThresholdValues>(
            0 to AlarmThresholdValues(0, 0, 0, 0, 0, 0),
            1 to AlarmThresholdValues(1, 0, 0, 0, 0, 0),
            2 to AlarmThresholdValues(2, 0, 0, 0, 0, 0),
            3 to AlarmThresholdValues(3, 0, 0, 0, 0, 0),
            4 to AlarmThresholdValues(4, 0, 0, 0, 0, 0),
            5 to AlarmThresholdValues(5, 0, 0, 0, 0, 0),
            6 to AlarmThresholdValues(6, 0, 0, 0, 0, 0),
            7 to AlarmThresholdValues(7, 0, 0, 0, 0, 0),
        )

    // TODO - Checken hoe om te gaan met uitroeptekens:
    //   - worden deze enkel aan het begin van de downlink gezet, of voor elk commando in de downlink?
    //   - hoe worden deze vervolgens teruggegeven in de downlink in de URC?
    fun getUrcListForDeviceMessage(): List<Any> = urcs + listOf(DeviceMessageDownlink(downlinks.joinToString(",")))

    fun resetUrc() {
        urcs.clear()
        downlinks.clear()
    }

    fun addUrc(urc: String) = apply { urcs += urc }

    fun addDownlink(downlink: String) = apply { downlinks += downlink }

    fun addAlarmThresholds(alarmThresholdValues: AlarmThresholdValues) = apply {
        alarmThresholds[alarmThresholdValues.index] = alarmThresholdValues
    }

    fun getAlarmThresholds() = alarmThresholds

    fun getAlarmThresholds(index: Int) = alarmThresholds[index]
}
