@file:Suppress("ClassName")
@file:OptIn(ExperimentalForeignApi::class)

package ksqlite.types

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import ksqlite.memory.Pointer

public actual class sqlite3_pointer private constructor(
    internal val pointer: COpaquePointer?,
    internal val size: Long
) {
    public companion object {
        internal fun from(pointer: COpaquePointer?, size: Long): sqlite3_pointer? {
            return pointer?.let { sqlite3_pointer(it, size) }
        }
    }
}

public actual class sqlite3 : Pointer<cnames.structs.sqlite3>()

public actual class sqlite3_context : Pointer<cnames.structs.sqlite3_context>()

public actual class sqlite3_stmt : Pointer<cnames.structs.sqlite3_stmt>()

public actual class sqlite3_value : Pointer<cnames.structs.sqlite3_value>()