package ksqlite.capi.handlers

import ksqlite.foreign.callbacks.ConfigLogCallback
import ksqlite.foreign.callbacks.ConfigSqlLogCallback
import ksqlite.capi.callbacks.SqliteConfigLogCallback
import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback
import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.capi.types.sqlite3

/**
 * Handler for the LOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigLogHandler<AppData> :
    Handler<SqliteConfigLogCallback<AppData>, AppData>(),
    ConfigLogCallback {

    override fun apply(
        errorCode: Int,
        message: String?
    ): Unit = handle { callback, appData ->
        callback.apply(
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
    Handler<SqliteConfigSqlLogCallback<AppData>, AppData>(),
    ConfigSqlLogCallback {

    override fun apply(
        db: Long,
        message: String?,
        messageType: Int
    ): Unit = handle { callback, appData ->
        dispatchSqlLogEvent(
            callback = callback,
            appData = appData,
            type = messageType,
            db = sqlite3(db),
            name = message
        )
    }
}