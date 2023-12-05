// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class PreSharedKey(@Id val identity: String, var preSharedKey: String)
