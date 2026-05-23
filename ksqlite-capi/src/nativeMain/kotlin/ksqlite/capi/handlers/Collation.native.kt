package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.callbacks.Sqlite3CreateCollationCallback
import ksqlite.capi.convertTextEncoding
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.s3
import ksqlite.capi.types.sqlite3

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
) = handler(refPointer) { callback: Sqlite3CollationNeededCallback<Any?>, appData ->
    callback.handle(
        appData = appData,
        db = sqlite3(db!!),
        eTextRep = convertTextEncoding(eTextRep),
        name = name!!.toKStringFromUtf8()
    )
}

///////////////////////////////////////////////////////////////////////////
// Create
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [createCollationHandler].
 */
internal val CreateCollationHandler = staticCFunction(::createCollationHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
private fun createCollationHandler(
    refPointer: COpaquePointer?,
    size1: Int,
    text1: COpaquePointer?,
    size2: Int,
    text2: COpaquePointer?
) = handler(refPointer) { callback: Sqlite3CreateCollationCallback<Any?>, appData ->
    callback.handle(
        appData = appData,
        left = text1!!.toKStringFromUtf8(size1),
        right = text2!!.toKStringFromUtf8(size2)
    )
}