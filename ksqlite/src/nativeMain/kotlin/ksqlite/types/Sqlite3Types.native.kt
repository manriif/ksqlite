@file:Suppress("ClassName")
@file:OptIn(ExperimentalForeignApi::class)

package ksqlite.types

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import ksqlite.memory.Pointer

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3 = cnames.structs.sqlite3
internal typealias s3_context = cnames.structs.sqlite3_context
internal typealias s3_stmt = cnames.structs.sqlite3_stmt
internal typealias s3_value = cnames.structs.sqlite3_value

///////////////////////////////////////////////////////////////////////////
// Types
///////////////////////////////////////////////////////////////////////////

public actual class sqlite3_pointer private constructor(
    internal val pointer: COpaquePointer?,
    internal val size: Long,
    internal val writable: Boolean
) {
    public companion object {
        internal fun from(
            pointer: COpaquePointer?,
            size: Long,
            writable: Boolean
        ): sqlite3_pointer? {
            return pointer?.let { sqlite3_pointer(it, size, writable) }
        }
    }
}

public actual class sqlite3(pointer: CPointer<s3>) : Pointer<s3>(pointer)

public actual class sqlite3_context(pointer: CPointer<s3_context>) : Pointer<s3_context>(pointer)

public actual class sqlite3_stmt(pointer: CPointer<s3_stmt>) : Pointer<s3_stmt>(pointer)

public actual class sqlite3_value(pointer: CPointer<s3_value>) : Pointer<s3_value>(pointer)