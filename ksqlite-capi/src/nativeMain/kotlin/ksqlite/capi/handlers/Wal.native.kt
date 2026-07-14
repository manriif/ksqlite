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
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.SqliteWalHookCallback
import ksqlite.capi.sqlite3
import ksqlite.capi.s3

/**
 * Static C function for [walHookHandler].
 */
internal val WalHookHandler = staticCFunction(::walHookHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_wal_hook].
 */
private fun walHookHandler(
    refPointer: COpaquePointer?,
    db: CPointer<s3>?,
    dbName: CPointer<ByteVar>?,
    nPage: Int,
) = handle(refPointer) { callback: SqliteWalHookCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        db = sqlite3(db!!),
        databaseName = dbName!!.toKStringFromUtf8(),
        pageCount = nPage
    ).code
}