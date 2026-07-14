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
@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.handlers

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.SqliteProgressHandlerCallback

/**
 * Static C function for [progressHandlerHandler].
 */
internal val ProgressHandlerHandler = staticCFunction(::progressHandlerHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
private fun progressHandlerHandler(
    refPointer: COpaquePointer?
) = handle(refPointer) { callback: SqliteProgressHandlerCallback<Any?>, appData ->
    callback.apply(appData)
}