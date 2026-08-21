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
package ksqlite.kapi

/**
 * SQLite API available without initializing a [SQLite] instance first.
 */
public interface SQLiteStatic {

    /**
     * Options SQLite was compiled with, each with its `SQLITE_` prefix omitted.
     */
    public val compileOptions: List<String>

    /**
     * Number of distinct keywords SQLite understands.
     */
    public val keywordCount: Int

    /**
     * SQLite version, for example `3.45.0`.
     */
    public val version: String

    /**
     * SQLite version encoded as a single integer, for example `3045000` for version `3.45.0`.
     */
    public val versionNumber: Int

    /**
     * Identifier of the check-in this SQLite build was produced from in its source control system.
     */
    public val sourceId: String

    /**
     * Version of SQLite Multiple Ciphers.
     */
    public val multipleCiphersVersion: String

    /**
     * Comparator for UTF-8 strings that compares them case-independently, using the same
     * definition of case independence SQLite uses internally when comparing identifiers.
     */
    public val caseIndependentComparator: Comparator<String>

    /**
     * Whether this SQLite build has mutexing code omitted because the `SQLITE_THREADSAFE`
     * compile-time option was set to `0`. When `true`, this library and its host application must
     * not use SQLite from more than one thread at a time.
     */
    public val isThreadSafe: Boolean

    /**
     * Returns `true` if [sql] looks like a complete SQL statement, or `false` if more input would
     * be needed before SQLite could parse it. Whitespace and comments after the final statement do
     * not count as needing more input.
     *
     * @throws SQLiteException if a memory allocation fails.
     */
    public fun isCompleteSqlStatement(sql: String): Boolean

    /**
     * Returns whether [word] is an SQLite keyword.
     */
    public fun isKeyword(word: String): Boolean

    /**
     * Returns the keyword at [index], out of the [keywordCount] keywords SQLite understands.
     *
     * @throws SQLiteException if [index] is out of bounds.
     */
    public fun getKeyword(index: Int): String

    /**
     * Sends [message] through SQLite's logging mechanism, invoking the currently configured
     * [ksqlite.kapi.config.Logger], if any, with [errorCode].
     */
    public fun log(
        errorCode: Int,
        message: String
    )

    /**
     * Returns whether [input] matches the GLOB [pattern].
     */
    public fun matchGlob(
        pattern: String,
        input: String
    ): Boolean

    /**
     * Returns whether [input] matches the LIKE [pattern], using [escape] as the escape character.
     */
    public fun matchLike(
        pattern: String,
        input: String,
        escape: Char
    ): Boolean

    /**
     * Returns a comparator for UTF-8 strings that compares them case-independently, the same way
     * [caseIndependentComparator] does, but only looking at the first [maxBytes] bytes.
     */
    public fun createCaseIndependentComparator(maxBytes: Int): Comparator<String>
}