@file:Suppress("ClassName")

package ksqlite

import java.lang.foreign.MemorySegment

public actual class pointer(internal val segment: MemorySegment)

public actual typealias sqlite3_context = pointer

public actual typealias sqlite3_stmt = pointer