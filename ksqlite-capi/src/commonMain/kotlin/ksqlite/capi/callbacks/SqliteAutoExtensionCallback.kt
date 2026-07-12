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

import ksqlite.capi.sqlite3
import ksqlite.types.SqliteResultCode

/**
 * Callback to use with [ksqlite.capi.sqlite3_auto_extension].
 */
public fun interface SqliteAutoExtensionCallback {

    /**
     * Result for [apply].
     */
    public sealed interface Result

    /**
     * Scope for [apply].
     */
    public sealed interface Scope {

        /**
         * Returns [SqliteResultCode.OK] to SQLite.
         */
        public fun success(): Result

        /**
         * Returns a failure [result] to SQLite and writes [message] to `pzErrMsg`.
         */
        public fun failure(
            result: SqliteResultCode.Failure,
            message: String
        ): Result
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/auto_extension.html).
     *
     * A [Result] instance can be obtained by invoking one of [Scope.success] or [Scope.failure].
     */
    public fun Scope.apply(db: sqlite3): Result
}