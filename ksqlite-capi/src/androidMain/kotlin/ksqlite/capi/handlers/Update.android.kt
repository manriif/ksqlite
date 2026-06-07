package ksqlite.capi.handlers

import ksqlite.callbacks.PreupdateHookCallback
import ksqlite.callbacks.UpdateHookCallback
import ksqlite.capi.callbacks.Sqlite3PreupdateHookCallback
import ksqlite.capi.callbacks.Sqlite3UpdateHookCallback
import ksqlite.capi.convertActionCode
import ksqlite.capi.types.sqlite3

/**
 * Handler for [ksqlite.capi.sqlite3_preupdate_hook].
 */
internal class PreupdateHookHandler<AppData> :
    Handler<Sqlite3PreupdateHookCallback<AppData>, AppData>(),
    PreupdateHookCallback {

    override fun apply(
        db: Long,
        op: Int,
        dbName: String,
        dbTable: String,
        iKey1: Long,
        iKey2: Long
    ) = handle { callback, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            action = convertActionCode(op),
            dbName = dbName,
            tableName = dbTable,
            preRowId = iKey1,
            postRowId = iKey2
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_update_hook].
 */
internal class UpdateHookHandler<AppData> :
    Handler<Sqlite3UpdateHookCallback<AppData>, AppData>(),
    UpdateHookCallback {

    override fun apply(
        opId: Int,
        dbName: String,
        tableName: String,
        rowId: Long
    ) = handle { callback, appData ->
        callback.apply(
            appData = appData,
            action = convertActionCode(opId),
            dbName = dbName,
            tableName = tableName,
            rowId = rowId
        )
    }
}