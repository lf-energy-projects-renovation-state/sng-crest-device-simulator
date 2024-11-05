// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.message

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(CrestNamingStrategy::class)
data class DeviceMessage(
    var a: List<Int> = listOf(3, 0, 0, 0, 0, 0, 0, 0),
    var bat: Int = 3758,
    var con: String = "M",
    var d: Int = 8,
    var eid: String = "89001012012341234012345678901224",
    var fmc: Int = 0,
    var fw: Int = 2100,
    var h1: List<Int> = listOf(463),
    var iccid: String = "89882280666074936745",
    var id: Long = 867787050253370,
    var imsi: Long = 460023210226023,
    var mem: Int = 0,
    var mid: Int = 1,
    var msi: Int = 0,
    var p1: List<Int> =
        listOf(
            2020,
            2034,
            2022,
            2050,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048),
    var p2: List<Int> =
        listOf(
            1800,
            1848,
            1948,
            2148,
            2248,
            1948,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048,
            2048),
    var pwr: Int = 1,
    var rly: Int = 0,
    var rsrp: Int = -99,
    var rsrq: Int = -210,
    var snr: Int = 22,
    var t1: List<Int> = listOf(222),
    var tel: Int = 20416,
    var `try`: Int = 1,
    var ts: Int = 1693318384,
    var tsl: Int = 1693318384,
    var upt: Int = 100,
    var urc: List<Any> = listOf("INIT", DeviceMessageDownlink()),
    @JsonProperty("cID") var cid: Int = 49093243
)

@JsonNaming(PropertyNamingStrategies.UpperSnakeCaseStrategy::class)
data class DeviceMessageDownlink(var dl: String = "0")