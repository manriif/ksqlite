package ksqlite.capi

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.destroyMemory
import ksqlite.capi.memory.memory
import ksqlite.types.SqliteResultCode

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes [block] which is expected to be the SQLite function that will deallocate [S] and
 * returns [block]'s result.
 *
 * If the deallocation succeeds, which is the case if [block] returns [SqliteResultCode.OK], then
 * all the resources associated with [S] through [memory] are disposed and [memory] is
 * closed before the function returns.
 */
internal inline fun <S> S.deallocate(block: (S) -> Int): SqliteResultCode
        where S : Struct, S : MemoryScope {
    val result = convertResult(block(this))

    if (result == SqliteResultCode.OK) {
        destroyMemory()
    }

    return result
}