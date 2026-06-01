package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback
import ksqlite.`sqlite3_commit_hook$x0`
import ksqlite.`sqlite3_rollback_hook$x0`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler :
    Handler(),
    `sqlite3_commit_hook$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_commit_hook$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment
    ): Int = handle(refPointer) { callback: Sqlite3CommitHookCallback<Any?>, appData ->
        callback.apply(appData)
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler :
    Handler(),
    `sqlite3_rollback_hook$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_rollback_hook$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment
    ): Unit = handle(refPointer) { callback: Sqlite3RollbackHookCallback<Any?>, appData ->
        callback.apply(appData)
    }
}