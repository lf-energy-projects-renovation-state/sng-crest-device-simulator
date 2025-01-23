// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.data.entity

data class AlarmThresholdValues(
    val index: Int,
    val veryLow: Int,
    val low: Int,
    val high: Int,
    val veryHigh: Int,
    val hysteresis: Int,
)
