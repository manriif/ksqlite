@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.SqlitePreupdateHookCallback
import ksqlite.capi.callbacks.SqliteUpdateHookCallback
import ksqlite.capi.sqlite3
import ksqlite.capi.s3
import ksqlite.foreign.sqlite3_int64
import ksqlite.types.internal.convertActionCode

///////////////////////////////////////////////////////////////////////////
// Preupdate
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [preupdateHookHandler].
 */
internal val PreupdateHookHandler = staticCFunction(::preupdateHookHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_preupdate_hook].
 */
private fun preupdateHookHandler(
    refPointer: COpaquePointer?,
    db: CPointer<s3>?,
    action: Int,
    dbName: CPointer<ByteVar>?,
    tableName: CPointer<ByteVar>?,
    iKey1: sqlite3_int64,
    ikey2: sqlite3_int64
) = handle(refPointer) { callback: SqlitePreupdateHookCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        db = sqlite3(db!!),
        action = convertActionCode(action),
        dbName = dbName!!.toKStringFromUtf8(),
        tableName = tableName!!.toKStringFromUtf8(),
        oldRowid = iKey1,
        newRowid = ikey2
    )
}

///////////////////////////////////////////////////////////////////////////
// Update
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [updateHookHandler].
 */
internal val UpdateHookHandler = staticCFunction(::updateHookHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_update_hook].
 */
private fun updateHookHandler(
    refPointer: COpaquePointer?,
    action: Int,
    dbName: CPointer<ByteVar>?,
    tableName: CPointer<ByteVar>?,
    rowId: sqlite3_int64
) = handle(refPointer) { callback: SqliteUpdateHookCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        action = convertActionCode(action),
        dbName = dbName!!.toKStringFromUtf8(),
        tableName = tableName!!.toKStringFromUtf8(),
        rowid = rowId
    )
}