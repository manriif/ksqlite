package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3WalHookCallback
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_wal_hook].
 */
internal class WalHookHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT
    )

    fun handle(
        refPointer: MemorySegment,
        db: MemorySegment,
        dbName: MemorySegment,
        nPage: Int,
    ): Int = handler(refPointer) { callback: Sqlite3WalHookCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            db = sqlite3(db),
            dbName = dbName.toKStringFromUtf8(),
            nPage = nPage
        ).code
    }
}