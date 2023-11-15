package org.gxf.crestdevicesimulator.simulator.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Psk(@Id val identity: String, @Column(name = "`key`") var key: String)
