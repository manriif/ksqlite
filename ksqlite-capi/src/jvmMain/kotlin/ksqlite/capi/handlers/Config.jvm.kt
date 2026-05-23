package ksqlite.capi.handlers

import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.callbacks.Sqlite3ConfigLogCallback
import ksqlite.capi.callbacks.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for the LOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigLogHandler<AppData>(manager: MemoryManager) : Handler<AppData>(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    fun handle(
        refPointer: MemorySegment,
        errCode: Int,
        errMsg: MemorySegment
    ): Unit = handler(refPointer) { callback: Sqlite3ConfigLogCallback<AppData>, appData ->
        callback.handle(
            appData = appData,
            errorCode = errCode,
            errorMsg = errMsg.toKStringFromUtf8OrNull()
        )
    }
}

/**
 * Handler for the SQLLOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigSqlLogHandler<AppData>(manager: MemoryManager) : Handler<AppData>(manager) {

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
    ): Unit = handler(refPointer) { callback: Sqlite3ConfigSqlLogCallback<AppData>, appData ->
        dispatchSqlLogEvent(
            callback = callback,
            appData = appData,
            type = type,
            db = sqlite3(db),
            name = name.toKStringFromUtf8OrNull()
        )
    }
}