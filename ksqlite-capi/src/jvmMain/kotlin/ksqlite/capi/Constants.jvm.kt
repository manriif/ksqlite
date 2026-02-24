package ksqlite.capi

import java.lang.foreign.MemorySegment

///////////////////////////////////////////////////////////////////////////
// Magic addresses
///////////////////////////////////////////////////////////////////////////

/**
 * Content pointer is constant and will never change and does not need to be destroyed.
 */
internal val SqliteStatic = MemorySegment.ofAddress(0L)

/**
 * Content will likely change in the near future and that SQLite should make its own private
 * copy of the content before returning.
 */
internal val SqliteTransient = MemorySegment.ofAddress(-1L)