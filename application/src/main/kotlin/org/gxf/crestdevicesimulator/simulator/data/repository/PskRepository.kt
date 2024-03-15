// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdevicesimulator.simulator.data.repository

import org.gxf.crestdevicesimulator.simulator.data.entity.PreSharedKey
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PskRepository : CrudRepository<PreSharedKey, PreSharedKeyCompositeKey> {
    @Query(
        """
        select psk from  PreSharedKey psk 
        where psk.identity = ?1 
          and psk.status = 'ACTIVE'
        order by psk.revision desc
        """
    )
    fun findLatestActivePsk(identity: String): PreSharedKey?
}
