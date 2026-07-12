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
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.value
import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_stmt

/**
 * Static C function for [traceHandler].
 */
internal val TraceHandler = staticCFunction(::traceHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
private fun traceHandler(
    code: UInt,
    refPointer: COpaquePointer?,
    pPointer: COpaquePointer?,
    xPointer: COpaquePointer?
) = handle(refPointer) { callback: SqliteTraceCallback<Any?>, appData ->
    dispatchTraceEvent(
        callback = callback,
        appData = appData,
        code = code.toInt(),
        pPointer = pPointer,
        xPointer = xPointer,
        toDb = { sqlite3(it.reinterpret()) },
        toStatement = { sqlite3_stmt(it.reinterpret()) },
        toString = { it.reinterpret<ByteVar>().toKStringFromUtf8() },
        toLong = { it.reinterpret<LongVar>().pointed.value }
    )
}