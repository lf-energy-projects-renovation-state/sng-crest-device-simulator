package org.gxf.crestdevicesimulator.simulator.data.repository

import org.gxf.crestdevicesimulator.simulator.data.entity.Psk
import org.springframework.data.repository.CrudRepository

interface PskRepository : CrudRepository<Psk, String>
