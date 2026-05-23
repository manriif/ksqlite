package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback
import ksqlite.capi.memory.MemoryManager
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler<AppData>(manager: MemoryManager) : Handler<AppData>(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    fun handle(
        refPointer: MemorySegment
    ): Int = handler(refPointer) { callback: Sqlite3CommitHookCallback<AppData>, appData ->
        callback.handle(appData)
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler<AppData>(manager: MemoryManager) : Handler<AppData>(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS
    )

    fun handle(
        refPointer: MemorySegment
    ): Unit = handler(refPointer) { callback: Sqlite3RollbackHookCallback<AppData>, appData ->
        callback.handle(appData)
    }
}