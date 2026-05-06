package ksqlite.capi.handlers

import ksqlite.capi.convertActionCode
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.types.Sqlite3PreupdateHookCallback
import ksqlite.capi.types.Sqlite3UpdateHookCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.memory.getStringUtf8
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_preupdate_hook].
 */
internal class PreupdateHookHandler(manager: MemoryManager) : Handler(manager) {

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
    ): Unit = handler(refPointer) { callback: Sqlite3PreupdateHookCallback, userData ->
        callback(
            userData,
            sqlite3(db),
            convertActionCode(action),
            dbName.getStringUtf8(),
            tableName.getStringUtf8(),
            oldRowId,
            newRowId
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_update_hook].
 */
internal class UpdateHookHandler(manager: MemoryManager) : Handler(manager) {

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
    ): Unit = handler(refPointer) { callback: Sqlite3UpdateHookCallback, userData ->
        callback(
            userData,
            convertActionCode(action),
            dbName.getStringUtf8(),
            tableName.getStringUtf8(),
            rowId
        )
    }
}