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
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.SqliteExecCallback
import ksqlite.capi.memory.toNullableStringArrayOrEmpty
import ksqlite.capi.memory.toStringArrayOrEmpty

/**
 * Static C function for [execHandler].
 */
internal val ExecHandler = staticCFunction(::execHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_exec].
 */
private fun execHandler(
    refPointer: COpaquePointer?,
    columnCount: Int,
    values: CPointer<CPointerVar<ByteVar>>?,
    names: CPointer<CPointerVar<ByteVar>>?
) = handle(refPointer) { callback: SqliteExecCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        columnCount = columnCount,
        columnValues = values.toNullableStringArrayOrEmpty(columnCount),
        columnNames = names.toStringArrayOrEmpty(columnCount)
    )
}