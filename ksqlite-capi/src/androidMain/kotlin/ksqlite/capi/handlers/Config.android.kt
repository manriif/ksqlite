/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteConfigLogCallback
import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback
import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.capi.sqlite3
import ksqlite.foreign.callbacks.ConfigLogCallback
import ksqlite.foreign.callbacks.ConfigSqlLogCallback

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