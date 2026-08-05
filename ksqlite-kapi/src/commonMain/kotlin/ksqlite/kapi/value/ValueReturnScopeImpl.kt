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
package ksqlite.kapi.value

import ksqlite.capi.sqlite3_result_blob
import ksqlite.capi.sqlite3_result_blob64
import ksqlite.capi.sqlite3_result_double
import ksqlite.capi.sqlite3_result_int
import ksqlite.capi.sqlite3_result_int64
import ksqlite.capi.sqlite3_result_null
import ksqlite.capi.sqlite3_result_pointer
import ksqlite.capi.sqlite3_result_text
import ksqlite.capi.sqlite3_result_text64
import ksqlite.capi.sqlite3_result_value
import ksqlite.capi.sqlite3_result_zeroblob
import ksqlite.capi.sqlite3_result_zeroblob64
import ksqlite.kapi.buffer.Buffer
import ksqlite.internal.runtime.closeable.ContextClosableScope
import ksqlite.kapi.helpers.autoCloser
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.types.SqliteTextEncoding

internal class ValueReturnScopeImpl(private val scope: ContextClosableScope) : ValueReturnScope {

    override fun setResult(value: Nothing?) =
        scope.notClosed { sqlite3_result_null(scope.context) }

    override fun setResult(value: Nothing?, size: Int) =
        scope.notClosed { sqlite3_result_zeroblob(scope.context, size) }

    override fun setResult(value: Nothing?, size: ULong) =
        scope.notClosed { sqliteResultCheck(sqlite3_result_zeroblob64(scope.context, size)) }

    override fun setResult(value: ByteArray, size: Int) =
        scope.notClosed { sqlite3_result_blob(scope.context, value, size, null) }

    override fun setResult(
        value: Buffer,
        size: Long,
        cleanup: ((Buffer) -> Unit)?
    ) = scope.notClosed {
        sqlite3_result_blob64(
            context = scope.context,
            buffer = value.buffer,
            size = size,
            destroy = value.reference(cleanup)
        )
    }

    override fun setResult(value: Int) =
        scope.notClosed { sqlite3_result_int(scope.context, value) }

    override fun setResult(value: Long) =
        scope.notClosed { sqlite3_result_int64(scope.context, value) }

    override fun setResult(value: Double) =
        scope.notClosed { sqlite3_result_double(scope.context, value) }

    override fun setResult(value: String) =
        scope.notClosed { sqlite3_result_text(scope.context, value) }

    override fun setResult(
        value: Buffer,
        encoding: SqliteTextEncoding.ResultText,
        size: Long,
        cleanup: ((Buffer) -> Unit)?
    ) = scope.notClosed {
        sqlite3_result_text64(
            context = scope.context,
            buffer = value.buffer,
            size = size,
            encoding = encoding,
            destroy = value.reference(cleanup)
        )
    }

    override fun setResult(value: Value) =
        scope.notClosed { sqlite3_result_value(scope.context, value.value) }

    override fun setResult(value: Any, type: String?) =
        scope.notClosed { sqlite3_result_pointer(scope.context, value, type, autoCloser(value)) }
}