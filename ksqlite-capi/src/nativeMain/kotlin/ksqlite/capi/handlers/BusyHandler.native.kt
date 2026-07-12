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

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.SqliteBusyHandlerCallback

/**
 * Static C function for [busyHandlerHandler].
 */
internal val BusyHandlerHandler = staticCFunction(::busyHandlerHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
private fun busyHandlerHandler(
    refPointer: COpaquePointer?,
    count: Int,
) = handle(refPointer) { callback: SqliteBusyHandlerCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        count = count
    )
}