// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.event

import org.gxf.crestdevicesimulator.simulator.message.DeviceMessage

data class MessageSentEvent(val message: DeviceMessage)
