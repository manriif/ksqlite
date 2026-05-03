package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.types.Sqlite3CommitHookCallback
import ksqlite.capi.types.Sqlite3RollbackHookCallback
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    fun handle(
        refPointer: MemorySegment
    ): Int = handler(refPointer) { callback: Sqlite3CommitHookCallback, userData ->
        callback(userData)
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS
    )

    fun handle(
        refPointer: MemorySegment
    ): Unit = handler(refPointer) { callback: Sqlite3RollbackHookCallback, userData ->
        callback(userData)
    }
}