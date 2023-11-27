// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.data.repository

import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKey
import org.springframework.data.repository.CrudRepository

interface PskRepository : CrudRepository<PreSharedKey, String>
