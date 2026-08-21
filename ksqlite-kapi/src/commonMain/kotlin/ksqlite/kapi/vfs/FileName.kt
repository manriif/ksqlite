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
package ksqlite.kapi.vfs

import ksqlite.kapi.helpers.sqliteList

/**
 * Exposes the [filename](https://sqlite.org/c3ref/filename.html) APIs.
 */
public interface FileName {

    /**
     * Filename content.
     */
    public val content: String

    /**
     * Name of the database file.
     */
    public val databaseFileName: String?

    /**
     * Name of the rollback journal file.
     */
    public val journalFileName: String?

    /**
     * Name of the WAL file.
     */
    public val walFileName: String?

    /**
     * Returns the name, not the value, of the 0-based [index]-th query parameter for this
     * filename, or `null` if [index] is out of bounds.
     */
    public fun getKey(index: Int): String?

    /**
     * Returns the value of the query [parameter], or `null` if it does not appear as a query
     * parameter of this filename. If [parameter] exists but has no explicit value, an empty
     * string is returned instead.
     */
    public fun getValue(parameter: String): String?

    /**
     * Returns `true` if the value of query [parameter] is one of "yes", "true", or "on" in any
     * case or if the value begins with a non-zero number.
     *
     * Returns `false` if the value of query [parameter] is one of "no", "false", or "off" in any
     * case or if the value begins with a numeric zero.
     *
     * If [parameter] is not present or if the value of [parameter] does not match any of the
     * above, then [default] is returned.
     */
    public fun getValue(
        parameter: String,
        default: Boolean
    ): Boolean

    /**
     * Converts the value of [parameter] into a 64-bit signed integer and returns that integer,
     * or [default] if [parameter] does not exist. If the value of [parameter] is something other
     * than an integer, then zero is returned.
     */
    public fun getValue(
        parameter: String,
        default: Long
    ): Long
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Lists all the keys of this [FileName].
 */
public fun FileName.keys(): List<String> = sqliteList(::getKey)