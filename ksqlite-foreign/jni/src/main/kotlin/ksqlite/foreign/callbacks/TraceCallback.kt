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
package ksqlite.foreign.callbacks

/**
 * Callback for use with [ksqlite.foreign.sqlite3_trace_v2].
 */
public fun interface TraceCallback {

    /**
     * Invoked from JNI.
     *
     * - [pPointer] is a [Long] pointing to a `sqlite3` or `sqlite3_stmt` depending on [code].
     * - [xPointer] is a [Long] or a [String] depending on [code].
     */
    public fun apply(
        code: Int,
        pPointer: Long,
        xPointer: Any?
    ): Int
}