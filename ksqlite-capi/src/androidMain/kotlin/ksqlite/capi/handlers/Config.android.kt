package ksqlite.capi.handlers

import ksqlite.ConfigLogCallback
import ksqlite.ConfigSqlLogCallback
import ksqlite.capi.callbacks.Sqlite3ConfigLogCallback
import ksqlite.capi.callbacks.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.capi.types.sqlite3

/**
 * Handler for the LOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigLogHandler<AppData> :
    Handler<Sqlite3ConfigLogCallback<AppData>, AppData>(),
    ConfigLogCallback {

    override fun call(
        errorCode: Int,
        message: String?
    ): Unit = handler { callback, appData ->
        callback.handle(
            appData = appData,
            errorCode = errorCode,
            message = message
        )
    }
}

/**
 * Handler for the SQLLOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigSqlLogHandler<AppData> :
    Handler<Sqlite3ConfigSqlLogCallback<AppData>, AppData>(),
    ConfigSqlLogCallback {

    override fun call(
        db: Long,
        message: String?,
        messageType: Int
    ): Unit = handler { callback, appData ->
        dispatchSqlLogEvent(
            callback = callback,
            appData = appData,
            type = messageType,
            db = sqlite3(db),
            name = message
        )
    }
}