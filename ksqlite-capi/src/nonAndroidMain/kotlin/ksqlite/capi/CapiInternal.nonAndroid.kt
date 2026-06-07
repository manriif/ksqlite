package ksqlite.capi

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.destroyMemory
import ksqlite.capi.memory.memory
import ksqlite.capi.types.Sqlite3Result

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes [block] which is expected to be the SQLite function that will deallocate [S] and
 * returns [block]'s result.
 *
 * If the deallocation succeeds, which is the case if [block] returns [Sqlite3Result.OK], then
 * all the resources associated with [S] through [memory] are disposed and [memory] is
 * closed before the function returns.
 */
internal inline fun <S> S.deallocate(block: (S) -> Int): Sqlite3Result
        where S : Struct, S : MemoryScope {
    val result = convertResult(block(this))

    if (result == Sqlite3Result.OK) {
        destroyMemory()
    }

    return result
}