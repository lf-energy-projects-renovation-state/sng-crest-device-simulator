package org.gxf.crestdevicesimulator.simulator.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Psk(@Id val identity: String, var key: String)
