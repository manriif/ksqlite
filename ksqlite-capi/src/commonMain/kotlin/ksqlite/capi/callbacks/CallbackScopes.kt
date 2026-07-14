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
package ksqlite.capi.callbacks

import ksqlite.types.SqliteResultCode

///////////////////////////////////////////////////////////////////////////
// Auto extension
///////////////////////////////////////////////////////////////////////////

internal data object AutoExtensionSuccessResult : SqliteAutoExtensionCallback.Result

internal data class AutoExtensionFailureResult(
    val result: SqliteResultCode.Failure,
    val message: String
) : SqliteAutoExtensionCallback.Result

/**
 * Implementation of [SqliteAutoExtensionCallback.Scope].
 */
internal object AutoExtensionCallbackScope : SqliteAutoExtensionCallback.Scope {

    override fun success(): SqliteAutoExtensionCallback.Result {
        return AutoExtensionSuccessResult
    }

    override fun failure(
        result: SqliteResultCode.Failure,
        message: String
    ): SqliteAutoExtensionCallback.Result {
        return AutoExtensionFailureResult(result, message)
    }
}