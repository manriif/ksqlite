package ksqlite.types

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

public actual open class Sqlite3IntBaseParam internal actual constructor(initialValue: Int) {

    private var detachedValue: Int = initialValue
    private var attachedValue: MemorySegment? = null

    internal actual open val rawValue: Int
        get() = attachedValue?.get(ValueLayout.JAVA_INT, 0) ?: detachedValue

    internal fun attach(arena: Arena): MemorySegment {
        val segment = arena.allocateFrom(ValueLayout.JAVA_INT, detachedValue)
        attachedValue = segment
        return segment
    }

    internal fun detach() {
        detachedValue = checkNotNull(attachedValue) { "Param is not attached" }
            .get(ValueLayout.JAVA_INT, 0)

        attachedValue = null
    }
}