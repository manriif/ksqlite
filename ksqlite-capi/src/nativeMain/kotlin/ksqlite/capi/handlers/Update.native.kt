@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.convertActionCode
import ksqlite.capi.callbacks.Sqlite3PreupdateHookCallback
import ksqlite.capi.callbacks.Sqlite3UpdateHookCallback
import ksqlite.capi.types.s3
import ksqlite.capi.types.sqlite3
import ksqlite.sqlite3_int64

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
    oldRowId: sqlite3_int64,
    newRowId: sqlite3_int64
) = handler(refPointer) { callback: Sqlite3PreupdateHookCallback, userData ->
    callback(
        userData,
        sqlite3(db!!),
        convertActionCode(action),
        dbName!!.toKStringFromUtf8(),
        tableName!!.toKStringFromUtf8(),
        oldRowId,
        newRowId
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
) = handler(refPointer) { callback: Sqlite3UpdateHookCallback, userData ->
    callback(
        userData,
        convertActionCode(action),
        dbName!!.toKStringFromUtf8(),
        tableName!!.toKStringFromUtf8(),
        rowId
    )
}