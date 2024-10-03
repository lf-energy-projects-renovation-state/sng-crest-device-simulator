// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdevicesimulator.simulator.response

import org.springframework.stereotype.Service

@Service
class CommandService {
    fun hasRebootCommand(command: String) = command.contains("CMD:REBOOT")
}
