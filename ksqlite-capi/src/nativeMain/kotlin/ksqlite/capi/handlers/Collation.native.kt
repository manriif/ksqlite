package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.SqliteCollationCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.types.s3
import ksqlite.capi.types.sqlite3
import ksqlite.types.internal.convertTextEncoding

///////////////////////////////////////////////////////////////////////////
// Collation
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [collationHandler].
 */
internal val CollationHandler = staticCFunction(::collationHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
private fun collationHandler(
    refPointer: COpaquePointer?,
    size1: Int,
    text1: COpaquePointer?,
    size2: Int,
    text2: COpaquePointer?
) = handle(refPointer) { callback: SqliteCollationCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        lhs = text1!!.readBytes(size1),
        rhs = text2!!.readBytes(size2)
    )
}

///////////////////////////////////////////////////////////////////////////
// Needed
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [collationNeededHandler].
 */
internal val CollationNeededHandler = staticCFunction(::collationNeededHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_collation_needed].
 */
private fun collationNeededHandler(
    refPointer: COpaquePointer?,
    db: CPointer<s3>?,
    eTextRep: Int,
    name: CPointer<ByteVar>?
) = handle(refPointer) { callback: SqliteCollationNeededCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        db = sqlite3(db!!),
        eTextRep = convertTextEncoding(eTextRep),
        name = name!!.toKStringFromUtf8()
    )
}