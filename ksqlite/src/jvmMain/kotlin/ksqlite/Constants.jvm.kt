package ksqlite

import java.lang.foreign.MemorySegment

///////////////////////////////////////////////////////////////////////////
// Magic addresses
///////////////////////////////////////////////////////////////////////////

internal val SqliteStatic = MemorySegment.ofAddress(SQLITE_STATIC.toLong())
internal val SqliteTransient = MemorySegment.ofAddress(SQLITE_TRANSIENT.toLong())