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
package ksqlite.capi

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import ksqlite.foreign.sqlite3_mprintf

/**
 * Static C function for [autoExtensionHandler].
 */
internal val AutoExtensionHandler = staticCFunction(::autoExtensionHandler)

/**
 * Handler for [sqlite3_auto_extension].
 */
private fun autoExtensionHandler(
    db: CPointer<s3>?,
    pzErrMsg: CPointer<CPointerVar<ByteVar>>?,
    pApi: CPointer<s3_api>?
) = autoExtensionHandle(
    db = sqlite3(db!!),
    api = pApi,
    errorPointer = pzErrMsg
) { errorPointer, message ->
    errorPointer.pointed.value = sqlite3_mprintf(message)
}