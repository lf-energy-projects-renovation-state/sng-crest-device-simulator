package org.gxf.crestdevicesimulator.simulator.response

import org.springframework.stereotype.Service

@Service
class CommandService {
    fun hasRebootCommand(command: String) =
        command.contains("CMD:REBOOT")
}