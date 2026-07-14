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
import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.types.internal.convertActionCode

/**
 * Static C function for [AuthorizerHandler].
 */
internal val AuthorizerHandler = staticCFunction(::authorizerHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
private fun authorizerHandler(
    refPointer: COpaquePointer?,
    action: Int,
    param3: CPointer<ByteVar>?,
    param4: CPointer<ByteVar>?,
    param5: CPointer<ByteVar>?,
    param6: CPointer<ByteVar>?
) = handle(refPointer) { callback: SqliteAuthorizerCallback<Any?>, appData ->
    callback.apply(
        appData = appData,
        action = convertActionCode(action),
        detail1 = param3?.toKStringFromUtf8(),
        detail2 = param4?.toKStringFromUtf8(),
        detail3 = param5?.toKStringFromUtf8(),
        detail4 = param6?.toKStringFromUtf8()
    ).code
}