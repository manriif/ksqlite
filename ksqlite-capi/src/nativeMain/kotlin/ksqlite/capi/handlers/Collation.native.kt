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

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.SqliteCollationCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.s3
import ksqlite.capi.sqlite3
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