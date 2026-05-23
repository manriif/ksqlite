package ksqlite.capi.handlers

import ksqlite.capi.convertActionCode
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.callbacks.Sqlite3PreupdateHookCallback
import ksqlite.capi.callbacks.Sqlite3UpdateHookCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.memory.toKStringFromUtf8
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_preupdate_hook].
 */
internal class PreupdateHookHandler<AppData>(manager: MemoryManager) : Handler<AppData>(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_LONG,
        ValueLayout.JAVA_LONG
    )

    fun handle(
        refPointer: MemorySegment,
        db: MemorySegment,
        action: Int,
        dbName: MemorySegment,
        tableName: MemorySegment,
        oldRowId: Long,
        newRowId: Long
    ): Unit = handler(refPointer) { callback: Sqlite3PreupdateHookCallback<AppData>, appData ->
        callback.handle(
            appData = appData,
            db = sqlite3(db),
            action = convertActionCode(action),
            dbName = dbName.toKStringFromUtf8(),
            tableName = tableName.toKStringFromUtf8(),
            oldRowId = oldRowId,
            newRowId = newRowId
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_update_hook].
 */
internal class UpdateHookHandler<AppData>(manager: MemoryManager) : Handler<AppData>(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_LONG
    )

    fun handle(
        refPointer: MemorySegment,
        action: Int,
        dbName: MemorySegment,
        tableName: MemorySegment,
        rowId: Long
    ): Unit = handler(refPointer) { callback: Sqlite3UpdateHookCallback<AppData>, appData ->
        callback.handle(
            appData = appData,
            action = convertActionCode(action),
            dbName = dbName.toKStringFromUtf8(),
            tableName = tableName.toKStringFromUtf8(),
            rowId = rowId
        )
    }
}