package org.gxf.crestdevicesimulator.simulator.data.repository

import java.io.Serializable

class PreSharedKeyCompositeKey(
    val identity: String?,
    val revision: Int?
) : Serializable {
    constructor() : this(null, null)
}
