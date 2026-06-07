package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3ConfigLogCallback
import ksqlite.capi.callbacks.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.types.sqlite3
import ksqlite.ksqlite_xLog
import ksqlite.ksqlite_xSqllog
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for the LOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigLogHandler :
    Handler(),
    ksqlite_xLog.Function {

    override fun allocate(arena: Arena): MemorySegment =
        ksqlite_xLog.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        errCode: Int,
        errMsg: MemorySegment
    ): Unit = handle(refPointer) { callback: Sqlite3ConfigLogCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            errorCode = errCode,
            message = errMsg.toKStringFromUtf8OrNull()
        )
    }
}

/**
 * Handler for the SQLLOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigSqlLogHandler :
    Handler(),
    ksqlite_xSqllog.Function {

    override fun allocate(arena: Arena): MemorySegment =
        ksqlite_xSqllog.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        db: MemorySegment,
        name: MemorySegment,
        type: Int
    ): Unit = handle(refPointer) { callback: Sqlite3ConfigSqlLogCallback<Any?>, appData ->
        dispatchSqlLogEvent(
            callback = callback,
            appData = appData,
            type = type,
            db = sqlite3(db),
            name = name.toKStringFromUtf8OrNull()
        )
    }
}