package ksqlite.capi.handlers

import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.types.Sqlite3ConfigLogCallback
import ksqlite.capi.types.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.memory.getStringUtf8OrNull
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for the LOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigLogHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    fun handle(
        refPointer: MemorySegment,
        errCode: Int,
        errMsg: MemorySegment
    ): Unit = handler(refPointer) { callback: Sqlite3ConfigLogCallback, userData ->
        callback(
            userData,
            errCode,
            errMsg.getStringUtf8OrNull()
        )
    }
}

/**
 * Handler for the SQLLOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigSqlLogHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT
    )

    fun handle(
        refPointer: MemorySegment,
        db: MemorySegment,
        name: MemorySegment,
        type: Int
    ): Unit = handler(refPointer) { callback: Sqlite3ConfigSqlLogCallback, userData ->
        dispatchSqlLogEvent(
            callback = callback,
            userData = userData,
            type = type,
            db = sqlite3(db),
            name = name.getStringUtf8OrNull()
        )
    }
}