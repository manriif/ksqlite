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

import ksqlite.foreign.JniPointer
import ksqlite.foreign.OutputPointer
import ksqlite.foreign.callbacks.AutoExtensionCallback

/**
 * Singleton handler for auto extensions.
 */
internal val AutoExtensionHandler by lazy {
    AutoExtensionCallback(::autoExtensionHandler)
}

/**
 * Handler for [sqlite3_auto_extension].
 * Dispatches sqlite3_auto_extension call to all registered extensions.
 */
private fun autoExtensionHandler(
    dbPtr: JniPointer,
    apiPtr: JniPointer,
    outErrMsg: OutputPointer.OfString
): Int = autoExtensionHandle(
    db = sqlite3(dbPtr),
    api = apiPtr,
    errorPointer = outErrMsg,
    setError = OutputPointer.OfString::value::set
)