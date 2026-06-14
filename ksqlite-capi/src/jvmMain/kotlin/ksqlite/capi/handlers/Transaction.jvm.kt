package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.capi.callbacks.SqliteRollbackHookCallback
import ksqlite.foreign.`sqlite3_commit_hook$x0`
import ksqlite.foreign.`sqlite3_rollback_hook$x0`
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
    ): Int = handle(refPointer) { callback: SqliteCommitHookCallback<Any?>, appData ->
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
    ): Unit = handle(refPointer) { callback: SqliteRollbackHookCallback<Any?>, appData ->
        callback.apply(appData)
    }
}