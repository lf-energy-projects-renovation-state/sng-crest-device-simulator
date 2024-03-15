// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.data.entity

import jakarta.persistence.*
import org.gxf.crestdevicesimulator.simulator.data.repository.PreSharedKeyCompositeKey

@Entity
@IdClass(PreSharedKeyCompositeKey::class)
class PreSharedKey(
    @Id val identity: String,
    @Id val revision: Int,
    var preSharedKey: String,
    val secret: String,
    @Enumerated(EnumType.STRING) var status: PreSharedKeyStatus
)

enum class PreSharedKeyStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    INVALID
}
