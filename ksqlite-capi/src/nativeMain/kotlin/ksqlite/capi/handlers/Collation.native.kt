package ksqlite.capi.handlers

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.convertTextEncoding
import ksqlite.capi.types.Sqlite3CollationCompareCallback
import ksqlite.capi.types.Sqlite3CollationNeededCallback
import ksqlite.capi.types.s3
import ksqlite.capi.types.sqlite3
import ksqlite.capi.utils.toKStringFromUtf8

///////////////////////////////////////////////////////////////////////////
// Needed
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [collationNeededHandler].
 */
internal val CollationNeededHandler = staticCFunction(::collationNeededHandler)

/**
 * Handler for [ksqlite.sqlite3_collation_needed].
 */
private fun collationNeededHandler(
    refPointer: COpaquePointer?,
    db: CPointer<s3>?,
    eTextRep: Int,
    name: CPointer<ByteVar>?
) = handler(refPointer) { callback: Sqlite3CollationNeededCallback, userData ->
    callback(
        userData,
        sqlite3(db!!),
        convertTextEncoding(eTextRep),
        name!!.toKStringFromUtf8()
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
 * Handler for [ksqlite.sqlite3_create_collation] and [ksqlite.capi.sqlite3_create_collation_v2].
 */
private fun createCollationHandler(
    refPointer: COpaquePointer?,
    size1: Int,
    text1: COpaquePointer?,
    size2: Int,
    text2: COpaquePointer?
) = handler(refPointer) { callback: Sqlite3CollationCompareCallback, userData ->
    callback(
        userData,
        text1!!.toKStringFromUtf8(size1),
        text2!!.toKStringFromUtf8(size2)
    )
}